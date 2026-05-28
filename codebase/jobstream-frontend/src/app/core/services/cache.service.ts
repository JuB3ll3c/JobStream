import { Injectable, inject } from '@angular/core';
import { JobDto, JobSearchResponse } from '../models';

/**
 * Interface pour une entrée de cache
 */
interface CacheEntry<T> {
  data: T;
  timestamp: number;
}

/**
 * Configuration du cache
 */
interface CacheConfig {
  maxSize: number;
  ttl: number; // Time to live en millisecondes
}

const DEFAULT_CONFIG: CacheConfig = {
  maxSize: 100,
  ttl: 5 * 60 * 1000 // 5 minutes
};

/**
 * Service de cache générique avec stratégie LRU et TTL
 */
@Injectable({
  providedIn: 'root'
})
export class CacheService {
  private readonly cache = new Map<string, CacheEntry<unknown>>();
  private readonly config: CacheConfig = DEFAULT_CONFIG;

  /**
   * Stocke une valeur dans le cache
   */
  set<T>(key: string, data: T): void {
    // Éviter que le cache dépasse la taille max
    if (this.cache.size >= this.config.maxSize) {
      this.evictOldest();
    }

    this.cache.set(key, {
      data,
      timestamp: Date.now()
    });
  }

  /**
   * Récupère une valeur du cache
   * Retourne null si expire ou inexistante
   */
  get<T>(key: string): T | null {
    const entry = this.cache.get(key);

    if (!entry) {
      return null;
    }

    // Vérifier l'expiration
    if (Date.now() - entry.timestamp > this.config.ttl) {
      this.cache.delete(key);
      return null;
    }

    // Mettre à jour l'ordre LRU
    this.cache.delete(key);
    this.cache.set(key, entry);

    return entry.data as T;
  }

  /**
   * Vérifie si une clé existe et est valide
   */
  has(key: string): boolean {
    return this.get(key) !== null;
  }

  /**
   * Supprime une entrée du cache
   */
  delete(key: string): void {
    this.cache.delete(key);
  }

  /**
   * Vide complètement le cache
   */
  clear(): void {
    this.cache.clear();
  }

  /**
   * Supprime les entrées correspondant à un pattern
   */
  invalidate(pattern: RegExp): void {
    const keysToDelete: string[] = [];
    
    this.cache.forEach((_, key) => {
      if (pattern.test(key)) {
        keysToDelete.push(key);
      }
    });

    keysToDelete.forEach(key => this.cache.delete(key));
  }

  /**
   * Récupère la taille actuelle du cache
   */
  get size(): number {
    return this.cache.size;
  }

  /**
   * Élimine l'entrée la plus ancienne (LRU)
   */
  private evictOldest(): void {
    if (this.cache.size === 0) return;

    // Trouver la plus ancienne
    let oldestKey: string | null = null;
    let oldestTime = Infinity;

    this.cache.forEach((entry, key) => {
      if (entry.timestamp < oldestTime) {
        oldestTime = entry.timestamp;
        oldestKey = key;
      }
    });

    if (oldestKey) {
      this.cache.delete(oldestKey);
    }
  }
}

/**
 * Cache spécialisé pour les jobs
 */
@Injectable({
  providedIn: 'root'
})
export class JobCacheService {
  private readonly cache = inject(CacheService);
  private readonly CACHE_PREFIX = 'job_search_';

  /**
   * Cache une réponse de recherche
   */
  setSearchResults(
    query: string, 
    page: number, 
    filters: Record<string, unknown> | undefined,
    response: JobSearchResponse
  ): void {
    const key = this.generateKey(query, page, filters);
    this.cache.set(key, response);
  }

  /**
   * Récupère une réponse de recherche depuis le cache
   */
  getSearchResults(
    query: string, 
    page: number, 
    filters: Record<string, unknown> | undefined
  ): JobSearchResponse | null {
    const key = this.generateKey(query, page, filters);
    return this.cache.get<JobSearchResponse>(key);
  }

  /**
   * Invalide le cache pour une requête
   */
  invalidateQuery(query: string): void {
    this.cache.invalidate(new RegExp(`^${this.CACHE_PREFIX}${this.escapeRegex(query)}_`));
  }

  /**
   * Vide tout le cache des jobs
   */
  clear(): void {
    this.cache.invalidate(new RegExp(`^${this.CACHE_PREFIX}`));
  }

  /**
   * Génère une clé de cache
   */
  private generateKey(
    query: string, 
    page: number, 
    filters: Record<string, unknown> | undefined
  ): string {
    const filterStr = filters ? JSON.stringify(filters) : '';
    return `${this.CACHE_PREFIX}${this.escapeRegex(query)}_${page}_${this.hash(filterStr)}`;
  }

  /**
   * Échappe les caractères spéciaux pour regex
   */
  private escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  /**
   * Simple hash pour les filtres
   */
  private hash(str: string): string {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash;
    }
    return Math.abs(hash).toString(36);
  }
}
