import { SALARY_CONVERSION_FACTOR, SALARY_UNIT } from './job.constants';

/**
 * Utilitaire pour formater les salaires
 * Respecte la règle TS010: Method size limits
 */
export class SalaryFormatter {
  /**
   * Formate un salaire en milliers (k)
   * @param salary Montant du salaire
   * @returns Salaire formaté (ex: "50k")
   */
  static formatToK(salary: number): string {
    return `$${(salary / SALARY_CONVERSION_FACTOR).toFixed(0)}${SALARY_UNIT}`;
  }

  /**
   * Formate une plage de salaire
   * @param minSalary Salaire minimum (peut être null ou undefined)
   * @param maxSalary Salaire maximum (peut être null ou undefined)
   * @returns Plage de salaire formatée
   */
  static formatSalaryRange(minSalary: number | null | undefined, maxSalary: number | null | undefined): string {
    // Normaliser undefined vers null
    const min = minSalary ?? null;
    const max = maxSalary ?? null;

    if (!min && !max) {
      return 'Salary not specified';
    }

    if (min && max) {
      return `${this.formatToK(min)} - ${this.formatToK(max)}`;
    }

    if (min) {
      return `${this.formatToK(min)}+`;
    }

    // max est forcément non-null ici grâce aux vérifications précédentes
    return `Up to ${this.formatToK(max!)}`;
  }

  /**
   * Vérifie si une plage de salaire est valide
   * @param min Salaire minimum
   * @param max Salaire maximum
   * @returns True si la plage est valide
   */
  static isValidSalaryRange(min: number, max: number): boolean {
    return min >= 0 && max >= 0 && min <= max;
  }

  /**
   * Normalise une plage de salaire
   * @param min Salaire minimum
   * @param max Salaire maximum
   * @returns Plage normalisée [min, max]
   */
  static normalizeSalaryRange(min: number, max: number): [number, number] {
    if (!this.isValidSalaryRange(min, max)) {
      return [Math.min(min, max), Math.max(min, max)];
    }
    return [min, max];
  }
}
