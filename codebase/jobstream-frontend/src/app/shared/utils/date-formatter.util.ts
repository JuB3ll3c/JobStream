/**
 * Utilitaire pour formater les dates et calculer les intervalles de temps
 */

export class DateFormatter {
  /**
   * Calcule le temps écoulé depuis une date
   * @param date Date de référence
   * @returns Temps écoulé formaté (ex: "2 hours ago")
   */
  static getTimeAgo(date: Date | string): string {
    if (!date) {
      return 'Recently';
    }
    
    const now = new Date();
    const pastDate = typeof date === 'string' ? new Date(date) : date;
    
    // Check if date is valid
    if (isNaN(pastDate.getTime())) {
      return 'Recently';
    }
    
    const diffInMs = now.getTime() - pastDate.getTime();

    const diffInSeconds = Math.floor(diffInMs / 1000);
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);
    const diffInWeeks = Math.floor(diffInDays / 7);
    const diffInMonths = Math.floor(diffInDays / 30);
    const diffInYears = Math.floor(diffInDays / 365);

    if (diffInYears > 0) {
      return `${diffInYears} year${diffInYears > 1 ? 's' : ''} ago`;
    }
    if (diffInMonths > 0) {
      return `${diffInMonths} month${diffInMonths > 1 ? 's' : ''} ago`;
    }
    if (diffInWeeks > 0) {
      return `${diffInWeeks} week${diffInWeeks > 1 ? 's' : ''} ago`;
    }
    if (diffInDays > 0) {
      return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    }
    if (diffInHours > 0) {
      return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    }
    if (diffInMinutes > 0) {
      return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
    }
    
    return 'Just now';
  }

  /**
   * Formate une date au format local
   * @param date Date à formater
   * @returns Date formatée (ex: "Jan 15, 2024")
   */
  static formatLocalDate(date: Date | string | undefined | null): string {
    if (!date) {
      return 'Recently';
    }
    
    let dateObj: Date;
    
    try {
      dateObj = typeof date === 'string' ? new Date(date) : date;
      
      // Check if date is valid - getTime() returns NaN for Invalid Date
      const time = dateObj.getTime();
      if (!Number.isFinite(time)) {
        return 'Recently';
      }
    } catch {
      return 'Recently';
    }
    
    return dateObj.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }

  /**
   * Vérifie si une date est valide
   * @param date Date à vérifier
   * @returns True si la date est valide
   */
  static isValidDate(date: Date | string): boolean {
    const dateObj = typeof date === 'string' ? new Date(date) : date;
    return !isNaN(dateObj.getTime());
  }
}