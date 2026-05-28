import { DateFormatter } from './date-formatter.util';

describe('DateFormatter', () => {
  describe('getTimeAgo', () => {
    it('should format seconds ago', () => {
      const date = new Date(Date.now() - 30 * 1000); // 30 seconds ago
      expect(DateFormatter.getTimeAgo(date)).toBe('Just now');
    });

    it('should format minutes ago', () => {
      const date = new Date(Date.now() - 5 * 60 * 1000); // 5 minutes ago
      expect(DateFormatter.getTimeAgo(date)).toBe('5 minutes ago');
    });

    it('should format hours ago', () => {
      const date = new Date(Date.now() - 3 * 60 * 60 * 1000); // 3 hours ago
      expect(DateFormatter.getTimeAgo(date)).toBe('3 hours ago');
    });

    it('should format days ago', () => {
      const date = new Date(Date.now() - 2 * 24 * 60 * 60 * 1000); // 2 days ago
      expect(DateFormatter.getTimeAgo(date)).toBe('2 days ago');
    });

    it('should format weeks ago', () => {
      const date = new Date(Date.now() - 3 * 7 * 24 * 60 * 60 * 1000); // 3 weeks ago
      expect(DateFormatter.getTimeAgo(date)).toBe('3 weeks ago');
    });

    it('should format months ago', () => {
      const date = new Date(Date.now() - 2 * 30 * 24 * 60 * 60 * 1000); // 2 months ago
      expect(DateFormatter.getTimeAgo(date)).toBe('2 months ago');
    });

    it('should format years ago', () => {
      const date = new Date(Date.now() - (365 * 24 * 60 * 60 * 1000 + 1000)); // 1 year ago
      expect(DateFormatter.getTimeAgo(date)).toBe('1 year ago');
    });
  });

  describe('formatLocalDate', () => {
    it('should format date correctly', () => {
      const date = new Date('2024-01-15');
      expect(DateFormatter.formatLocalDate(date)).toBe('Jan 15, 2024');
    });

    it('should handle string date', () => {
      expect(DateFormatter.formatLocalDate('2024-01-15')).toBe('Jan 15, 2024');
    });
  });

  describe('isValidDate', () => {
    it('should validate valid date', () => {
      expect(DateFormatter.isValidDate(new Date())).toBe(true);
      expect(DateFormatter.isValidDate('2024-01-15')).toBe(true);
    });

    it('should reject invalid date', () => {
      expect(DateFormatter.isValidDate('invalid-date')).toBe(false);
      expect(DateFormatter.isValidDate(new Date('invalid'))).toBe(false);
    });
  });
});