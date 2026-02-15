import { useState } from 'react';
import {
  Box,
  Button,
  ButtonGroup,
  Flex,
  IconButton,
  Input,
  Pagination,
  Spinner,
  Table,
  Text,
} from '@chakra-ui/react';
import { ChevronLeft, ChevronRight, Search } from 'lucide-react';
import type { VehicleDto } from '../../api/models/vehicles';
import { useStores } from '../../store';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const PAGE_SIZE = 10;

const SearchByNameSection = () => {
  const { vehiclesStore } = useStores();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<VehicleDto[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPage = async (targetPage: number) => {
    if (!query.trim()) {
      setError('Введите подстроку имени');
      return;
    }
    try {
      setLoading(true);
      setError(null);
      const offset = targetPage - 1;
      const data = await vehiclesStore.searchByName(
        query.trim(),
        offset,
        PAGE_SIZE,
      );
      setResults(data.items);
      setTotal(data.total);
      setPage(targetPage);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      setError(message ?? 'Не удалось выполнить поиск');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    loadPage(1);
  };

  const pagesCount = total > 0 ? Math.ceil(total / PAGE_SIZE) : 0;

  return (
    <Box borderWidth="1px" borderRadius="md" p={4}>
      <Flex align="center" gap={2} mb={3}>
        <Search size={18} />
        <Text fontSize="lg" fontWeight="semibold">
          Поиск ТС по подстроке в имени
        </Text>
      </Flex>

      <Flex gap={4} align="flex-end" flexWrap="wrap" mb={3}>
        <Box minW="260px">
          <Text mb={1}>Подстрока имени</Text>
          <Input
            size="sm"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setError(null);
            }}
            placeholder="Например, car"
          />
        </Box>

        <Button size="sm" onClick={handleSearch} disabled={loading}>
          Найти
        </Button>
      </Flex>

      {loading && (
        <Flex align="center" gap={2} mt={2}>
          <Spinner size="sm" />
          <Text>Загрузка...</Text>
        </Flex>
      )}

      {error && (
        <Text mt={2} fontSize="sm" color="red.500">
          {error}
        </Text>
      )}

      {results.length > 0 && (
        <>
          <Table.Root size="sm" mt={3}>
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>Название</Table.ColumnHeader>
                <Table.ColumnHeader>Мощность</Table.ColumnHeader>
                <Table.ColumnHeader>Тип топлива</Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {results.map((v) => (
                <Table.Row key={v.id}>
                  <Table.Cell>{v.id}</Table.Cell>
                  <Table.Cell>{v.name}</Table.Cell>
                  <Table.Cell>{v.enginePower}</Table.Cell>
                  <Table.Cell>{v.fuelType ?? '—'}</Table.Cell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table.Root>

          {pagesCount > 1 && (
            <Flex justify="center" mt={3}>
              <Pagination.Root
                count={total}
                pageSize={PAGE_SIZE}
                page={page}
                onPageChange={(details: any) =>
                  loadPage(details.page as number)
                }
              >
                <ButtonGroup variant="ghost" size="sm">
                  <Pagination.PrevTrigger asChild>
                    <IconButton aria-label="Предыдущая страница">
                      <ChevronLeft size={16} />
                    </IconButton>
                  </Pagination.PrevTrigger>

                  <Pagination.Items
                    render={(p) => (
                      <IconButton
                        key={p.value}
                        variant={
                          {
                            base: 'ghost',
                            _selected: 'outline',
                          } as any
                        }
                      >
                        {p.value}
                      </IconButton>
                    )}
                  />

                  <Pagination.NextTrigger asChild>
                    <IconButton aria-label="Следующая страница">
                      <ChevronRight size={16} />
                    </IconButton>
                  </Pagination.NextTrigger>
                </ButtonGroup>
              </Pagination.Root>
            </Flex>
          )}
        </>
      )}
    </Box>
  );
};

export default SearchByNameSection;

