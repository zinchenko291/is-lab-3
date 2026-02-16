import { useEffect, useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate, useParams } from 'react-router';
import {
  Box,
  Button,
  Flex,
  Spinner,
  Table,
  Text,
} from '@chakra-ui/react';
import { ArrowLeft } from 'lucide-react';

import { useStores } from '../../store';
import { toaster } from '../../components/ui/toaster';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';
import {
  ImportConflictResolution,
  importConflictResolutionLabels,
} from '../../api/models/imports';
import Date from '../../components/Date';

const ImportConflictsPage = observer(() => {
  const { importsStore } = useStores();
  const navigate = useNavigate();
  const params = useParams();

  const importId = useMemo(() => Number(params.id), [params.id]);
  const [resolvingIds, setResolvingIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (!Number.isFinite(importId)) return;
    importsStore.loadConflicts(importId);
    return () => {
      importsStore.clearConflicts();
    };
  }, [importId, importsStore]);

  const operation = Number.isFinite(importId)
    ? importsStore.getOperationById(importId)
    : null;

  const setResolving = (id: number, value: boolean) => {
    setResolvingIds((prev) => {
      const next = new Set(prev);
      if (value) {
        next.add(id);
      } else {
        next.delete(id);
      }
      return next;
    });
  };

  const handleResolve = async (conflictId: number, action: 'SKIP' | 'OVERWRITE') => {
    try {
      setResolving(conflictId, true);
      await importsStore.resolveConflict(importId, conflictId, action);
      toaster.create({
        title: 'Конфликт обработан',
        type: 'success',
      });
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      toaster.create({
        title: message ?? 'Не удалось обработать конфликт',
        type: 'error',
      });
    } finally {
      setResolving(conflictId, false);
    }
  };

  if (!Number.isFinite(importId)) {
    return (
      <Box p={6}>
        <Text>Некорректный идентификатор импорта.</Text>
      </Box>
    );
  }

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Box>
          <Text fontSize="2xl" fontWeight="bold">
            Конфликты импорта #{importId}
          </Text>
          {operation && (
            <Text color="gray.500" mt={1}>
              Статус: {operation.status}
            </Text>
          )}
        </Box>
        <Button variant="ghost" onClick={() => navigate(-1)}>
          <Flex align="center" gap={2}>
            <ArrowLeft size={16} />
            <Text>Назад</Text>
          </Flex>
        </Button>
      </Flex>

      <Box
        borderWidth="1px"
        borderRadius="md"
        overflowX="auto"
        position="relative"
        minH="220px"
      >
        {importsStore.conflictsError && (
          <Text mb={3} fontSize="sm" color="red.500">
            {importsStore.conflictsError}
          </Text>
        )}

        {importsStore.conflictsLoading && (
          <Flex
            position="absolute"
            inset={0}
            bg="blackAlpha.50"
            zIndex={1}
            justify="center"
            align="center"
            gap={3}
          >
            <Spinner size="sm" />
            <Text>Загрузка...</Text>
          </Flex>
        )}

        {!importsStore.conflictsLoading &&
          importsStore.conflicts.length === 0 && (
            <Flex justify="center" align="center" p={4} minH="220px">
              <Text>Конфликты не найдены</Text>
            </Flex>
          )}

        {importsStore.conflicts.length > 0 && (
          <Table.Root
            size="sm"
            opacity={importsStore.conflictsLoading ? 0.6 : 1}
          >
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>Статус</Table.ColumnHeader>
                <Table.ColumnHeader>Индекс ТС</Table.ColumnHeader>
                <Table.ColumnHeader>Существующий ID</Table.ColumnHeader>
                <Table.ColumnHeader>X</Table.ColumnHeader>
                <Table.ColumnHeader>Y</Table.ColumnHeader>
                {/*<Table.ColumnHeader>Пользователь</Table.ColumnHeader>*/}
                <Table.ColumnHeader>Создан</Table.ColumnHeader>
                <Table.ColumnHeader textAlign="end">
                  Действия
                </Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {importsStore.conflicts.map((conflict) => {
                const isResolving = resolvingIds.has(conflict.id);
                const isUnresolved =
                  conflict.resolution === ImportConflictResolution.UNRESOLVED;

                return (
                  <Table.Row key={conflict.id}>
                    <Table.Cell>{conflict.id}</Table.Cell>
                    <Table.Cell>
                      {importConflictResolutionLabels[conflict.resolution]}
                    </Table.Cell>
                    <Table.Cell>{conflict.vehicleIndex}</Table.Cell>
                    <Table.Cell>{conflict.existingVehicleId ?? '—'}</Table.Cell>
                    <Table.Cell>{conflict.coordinateX}</Table.Cell>
                    <Table.Cell>{conflict.coordinateY}</Table.Cell>
                    {/*<Table.Cell>{conflict.userId}</Table.Cell>*/}
                    <Table.Cell>
                      <Date>{conflict.createdAt}</Date>
                    </Table.Cell>
                    <Table.Cell textAlign="end">
                      <Flex justify="flex-end" gap={2}>
                        <Button
                          size="xs"
                          variant="outline"
                          disabled={!isUnresolved || isResolving}
                          onClick={() => handleResolve(conflict.id, 'SKIP')}
                        >
                          Пропустить
                        </Button>
                        <Button
                          size="xs"
                          colorScheme="blue"
                          disabled={!isUnresolved || isResolving}
                          onClick={() => handleResolve(conflict.id, 'OVERWRITE')}
                        >
                          Перезаписать
                        </Button>
                      </Flex>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table.Root>
        )}
      </Box>
    </Box>
  );
});

export default ImportConflictsPage;

