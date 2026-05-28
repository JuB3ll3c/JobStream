import { SalaryFormatter } from './salary-formatter.util';

describe('SalaryFormatter', () => {
  describe('formatToK', () => {
    it('should format salary to thousands', () => {
      expect(SalaryFormatter.formatToK(50000)).toBe('$50k');
      expect(SalaryFormatter.formatToK(75000)).toBe('$75k');
      expect(SalaryFormatter.formatToK(100000)).toBe('$100k');
    });

    it('should round salary correctly', () => {
      expect(SalaryFormatter.formatToK(55000)).toBe('$55k');
      expect(SalaryFormatter.formatToK(55500)).toBe('$55k');
      expect(SalaryFormatter.formatToK(55999)).toBe('$56k');
    });
  });

  describe('formatSalaryRange', () => {
    it('should format both min and max salary', () => {
      expect(SalaryFormatter.formatSalaryRange(50000, 80000)).toBe('$50k - $80k');
    });

    it('should format only min salary', () => {
      expect(SalaryFormatter.formatSalaryRange(60000, null)).toBe('$60k+');
    });

    it('should format only max salary', () => {
      expect(SalaryFormatter.formatSalaryRange(null, 90000)).toBe('Up to $90k');
    });

    it('should handle no salary', () => {
      expect(SalaryFormatter.formatSalaryRange(null, null)).toBe('Salary not specified');
    });
  });

  describe('isValidSalaryRange', () => {
    it('should validate correct salary range', () => {
      expect(SalaryFormatter.isValidSalaryRange(50000, 80000)).toBe(true);
      expect(SalaryFormatter.isValidSalaryRange(0, 100000)).toBe(true);
    });

    it('should reject invalid salary range', () => {
      expect(SalaryFormatter.isValidSalaryRange(80000, 50000)).toBe(false);
      expect(SalaryFormatter.isValidSalaryRange(-10000, 50000)).toBe(false);
      expect(SalaryFormatter.isValidSalaryRange(50000, -10000)).toBe(false);
    });
  });

  describe('normalizeSalaryRange', () => {
    it('should normalize valid range', () => {
      expect(SalaryFormatter.normalizeSalaryRange(50000, 80000)).toEqual([50000, 80000]);
    });

    it('should normalize invalid range', () => {
      expect(SalaryFormatter.normalizeSalaryRange(80000, 50000)).toEqual([50000, 80000]);
      expect(SalaryFormatter.normalizeSalaryRange(100000, 50000)).toEqual([50000, 100000]);
    });
  });
});