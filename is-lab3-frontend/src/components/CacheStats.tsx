import React, { useCallback, useEffect, useRef, useState } from 'react'
import type { L2CacheStatsSnapshot } from '../api/models/L2CacheStatsSnapshot';
import { CacheService } from '../api/services/cacheService';
import { HStack, Text } from '@chakra-ui/react';

const CacheStats = () => {
  const timer = useRef<ReturnType<typeof setInterval> | null>(null);
  const [stats, setStats] = useState<L2CacheStatsSnapshot | undefined>();

  const updateStats = useCallback(async () => {
    try {
      const stats = await CacheService.getStats();
      setStats(stats);
    } catch (e) { console.error(e) }
  }, []);

  useEffect(() => {
    if (timer.current) return;
    void updateStats();

    timer.current = setInterval(() => {
      updateStats();
    }, 5000);

    return () => {
      timer.current && clearInterval(timer.current);
    }
  }, []);

  if (!stats) return null;

  return (
    <HStack>
      <Text>CH: {stats.hitCount}</Text>
      <Text>CM: {stats.missCount}</Text>
    </HStack>
  )
}

export default CacheStats