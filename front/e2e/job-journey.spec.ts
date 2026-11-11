import { expect, test } from '@playwright/test';
import type { JobDto } from '../src/app/generated/model/jobDto';

test('search, save, track and remove an offer through the shared modal', async ({
  page,
}, testInfo) => {
  const offer: JobDto = {
    externalId: 'e2e-java-1',
    title: 'Java Engineer',
    company: 'Example Company',
    location: 'Zurich',
    description: 'Build useful software with Java and Spring.',
    salaryMin: 80000,
    salaryMax: 100000,
    contractType: 'Permanent',
    postedDate: '2026-11-01',
    requirements: ['Java', 'Spring'],
    jobUrl: 'https://example.com/jobs/e2e-java-1',
  };
  let saved: JobDto | null = null;
  const mutations: string[] = [];
  const unexpected: string[] = [];

  // All API traffic is intercepted: this test never contacts the real backend.
  await page.route('**/api/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const method = request.method();
    const path = url.pathname;

    if (method === 'POST' && path === '/api/auth/login') {
      expect(request.postDataJSON()).toEqual({
        email: 'candidate@example.com',
        password: 'test-password',
      });
      await route.fulfill({
        json: { accessToken: 'e2e-token', tokenType: 'Bearer', role: 'USER' },
      });
      return;
    }
    expect(request.headers()['authorization']).toBe('Bearer e2e-token');
    if (method === 'GET' && path === '/api/job-offers') {
      expect(url.searchParams.get('title')).toBe('Java');
      expect(url.searchParams.get('location')).toBe('Zurich');
      await route.fulfill({
        json: {
          content: [offer],
          page: 1,
          size: 20,
          totalElements: 1,
          totalPages: 1,
        },
      });
      return;
    }
    if (method === 'POST' && path === '/api/jobs') {
      expect(request.postDataJSON()).toMatchObject({
        externalId: 'e2e-java-1',
        title: 'Java Engineer',
        company: 'Example Company',
      });
      saved = { ...request.postDataJSON(), id: 7, status: 'TO_PROCESS' };
      mutations.push('save');
      await route.fulfill({ status: 201, json: saved });
      return;
    }
    if (method === 'GET' && path === '/api/jobs') {
      await route.fulfill({
        json: {
          content: saved ? [saved] : [],
          page: 0,
          size: 100,
          totalElements: saved ? 1 : 0,
          totalPages: saved ? 1 : 0,
        },
      });
      return;
    }
    if (method === 'PATCH' && path === '/api/jobs/7/status' && saved) {
      expect(request.postDataJSON()).toEqual({ status: 'APPLIED' });
      saved = { ...saved, status: 'APPLIED' };
      mutations.push('status:APPLIED');
      await route.fulfill({ json: saved });
      return;
    }
    if (method === 'DELETE' && path === '/api/jobs/7' && saved) {
      saved = null;
      mutations.push('remove');
      await route.fulfill({ status: 204 });
      return;
    }
    unexpected.push(method + ' ' + path);
    await route.fulfill({ status: 501, json: { message: 'Unexpected API request in E2E test' } });
  });

  await page.goto('/jobs');
  await expect(page).toHaveURL(/\/login$/);
  await expect(
    page.getByRole('navigation', { name: 'Main navigation' }).getByRole('link'),
  ).toHaveCount(0);
  await page.getByLabel('Email', { exact: true }).fill('candidate@example.com');
  await page.getByLabel('Password', { exact: true }).fill('test-password');
  await page.getByRole('button', { name: 'Login', exact: true }).click();
  await expect(page).toHaveURL(/\/jobs$/);
  await expect(page.getByRole('link', { name: 'Mes offres', exact: true })).toBeVisible();

  await page.getByLabel('Poste ou mots-clés').fill('Java');
  await page.getByLabel('Localité', { exact: true }).fill('Zurich');
  await page.getByRole('button', { name: 'Rechercher', exact: true }).click();
  const searchOpener = page.getByRole('button', { name: 'Java Engineer', exact: true });
  await expect(searchOpener).toBeVisible();
  const searchURL = page.url();
  await searchOpener.click();
  const detail = page.getByRole('dialog', { name: 'Java Engineer', exact: true });
  const close = detail.getByRole('button', { name: 'Fermer le détail' });
  await expect(detail).toBeVisible();
  await expect(close).toBeFocused();
  await expect(detail.getByRole('heading', { name: 'Java Engineer', exact: true })).toBeVisible();
  await expect(detail).toBeInViewport();
  const bounds = await detail.boundingBox();
  const viewport = page.viewportSize()!;
  expect(bounds).not.toBeNull();
  expect(bounds!.x).toBeGreaterThanOrEqual(0);
  expect(bounds!.y).toBeGreaterThanOrEqual(0);
  expect(bounds!.x + bounds!.width).toBeLessThanOrEqual(viewport.width);
  expect(bounds!.y + bounds!.height).toBeLessThanOrEqual(viewport.height);
  // Native dialogs may send reverse tab navigation to browser chrome.
  // Check the page's inert background and forward keyboard navigation instead.
  await page.getByLabel('Poste ou mots-clés').focus();
  await expect(close).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(
    detail.getByRole('button', { name: 'Sauvegarder Java Engineer', exact: true }),
  ).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(detail.getByRole('link', { name: 'Voir l’annonce originale' })).toBeFocused();
  await detail.screenshot({ path: testInfo.outputPath('search-detail.png') });
  await testInfo.attach('search-detail', {
    path: testInfo.outputPath('search-detail.png'),
    contentType: 'image/png',
  });
  await page.keyboard.press('Escape');
  await expect.soft(detail, 'Escape closes the offer detail').toHaveCount(0);
  // Continue the journey even if Escape is broken; the soft assertion still fails the test.
  if (await detail.count()) await close.click();
  await expect(searchOpener).toBeFocused();
  expect(page.url()).toBe(searchURL);

  await searchOpener.click();
  const save = detail.getByRole('button', { name: 'Sauvegarder Java Engineer', exact: true });
  await save.click();
  await expect(
    detail.getByRole('button', { name: 'Retirer des offres sauvegardées : Java Engineer' }),
  ).toHaveAttribute('aria-pressed', 'true');
  await close.click();
  await expect(searchOpener).toBeFocused();

  await page.getByRole('link', { name: 'Mes offres', exact: true }).click();
  await expect(page).toHaveURL(/\/saved-jobs$/);
  const boardOpener = page.getByRole('button', { name: 'Java Engineer', exact: true });
  await boardOpener.click();
  await expect(close).toBeFocused();
  await expect(detail.getByLabel('Statut de la candidature')).toHaveValue('TO_PROCESS');
  await detail.getByLabel('Statut de la candidature').selectOption('APPLIED');
  await expect(detail.getByLabel('Statut de la candidature')).toHaveValue('APPLIED');
  await expect(
    detail.getByRole('status').filter({ hasText: 'Statut de « Java Engineer » enregistré.' }),
  ).toBeVisible();
  await expect(
    page.locator('[data-status="APPLIED"]').getByRole('button', {
      name: 'Java Engineer',
      exact: true,
    }),
  ).toHaveCount(1);
  await detail.screenshot({ path: testInfo.outputPath('saved-detail.png') });
  await testInfo.attach('saved-detail', {
    path: testInfo.outputPath('saved-detail.png'),
    contentType: 'image/png',
  });

  const remove = detail.getByRole('button', {
    name: 'Retirer des offres sauvegardées : Java Engineer',
  });
  const confirmation = page.getByRole('dialog', {
    name: 'Retirer « Java Engineer » de vos offres ?',
  });
  await remove.click();
  await expect(confirmation).toBeVisible();
  await expect(confirmation.getByRole('button', { name: 'Annuler' })).toBeFocused();
  await confirmation.screenshot({ path: testInfo.outputPath('removal-confirmation.png') });
  await testInfo.attach('removal-confirmation', {
    path: testInfo.outputPath('removal-confirmation.png'),
    contentType: 'image/png',
  });
  await page.keyboard.press('Escape');
  await expect.soft(confirmation, 'Escape cancels the removal confirmation').not.toBeVisible();
  if (await confirmation.isVisible())
    await confirmation.getByRole('button', { name: 'Annuler' }).click();
  await expect(remove).toBeFocused();
  expect(mutations).toEqual(['save', 'status:APPLIED']);

  await remove.click();
  await confirmation.getByRole('button', { name: 'Annuler' }).click();
  await expect(confirmation).not.toBeVisible();
  await expect(remove).toBeFocused();
  expect(mutations).toEqual(['save', 'status:APPLIED']);

  await remove.click();
  await confirmation.getByRole('button', { name: 'Retirer', exact: true }).click();
  await expect(confirmation).not.toBeVisible();
  await expect(detail).toHaveCount(0);
  await expect
    .soft(
      page.getByRole('heading', { name: 'Mes offres', exact: true }),
      'Focus returns to the board heading after removing the last offer',
    )
    .toBeFocused();
  await expect(page.getByText('Aucune offre sauvegardée.', { exact: false })).toBeVisible();
  await expect(boardOpener).toHaveCount(0);
  expect(mutations).toEqual(['save', 'status:APPLIED', 'remove']);
  expect(unexpected).toEqual([]);
});
