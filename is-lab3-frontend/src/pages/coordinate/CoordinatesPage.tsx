import { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate } from 'react-router';
import {
  Box,
  Button,
  ButtonGroup,
  Flex,
  IconButton,
  Spinner,
  Text,
  Table,
  Pagination,
} from '@chakra-ui/react';
import { ChevronLeft, ChevronRight, Eye, Pencil, Trash2 } from 'lucide-react';

import { useStores } from '../../store';
import CoordinatesFilter from '../../components/CoordinatesFilter.tsx';
import { toaster } from '../../components/ui/toaster.tsx';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const CoordinatesPage = observer(() => {
  const { coordinatesStore } = useStores();
  const navigate = useNavigate();

  useEffect(() => {
    coordinatesStore.loadCoordinates(0);
  }, [coordinatesStore]);

  const handlePageChange = (details: any) => {
    const nextPage = details.page as number;
    coordinatesStore.goToPage(nextPage - 1);
  };

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm(
      `Вы действительно хотите удалить координату #${id}?`,
    );
    if (!confirmed) return;

    try {
      await coordinatesStore.deleteCoordinates(id);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      toaster.create({
        title: message ?? 'Вознилка непредвиденная ошибка',
        type: 'error'
      });
    }
  };

  const totalItems = coordinatesStore.total;
  const pageSize = coordinatesStore.pageSize;
  const currentPage = coordinatesStore.currentPage + 1;

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Координаты
        </Text>
        <Button
          colorScheme="teal"
          onClick={() => navigate('/coordinates/create')}
        >
          Создать координаты
        </Button>
      </Flex>

      <CoordinatesFilter
        initialField={coordinatesStore.filters.field}
        initialValue={coordinatesStore.filters.value}
        initialOrderBy={coordinatesStore.filters.orderBy}
      />

      <Box
        borderWidth="1px"
        borderRadius="md"
        overflowX="auto"
        position="relative"
        minH="180px"
      >
        {coordinatesStore.loading && (
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

        {!coordinatesStore.loading &&
          coordinatesStore.coordinates.length === 0 && (
            <Flex justify="center" align="center" p={4} minH="180px">
              <Text>Координаты не найдены</Text>
            </Flex>
          )}

        {coordinatesStore.coordinates.length > 0 && (
          <Table.Root
            size="sm"
            opacity={coordinatesStore.loading ? 0.6 : 1}
          >
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>X</Table.ColumnHeader>
                <Table.ColumnHeader>Y</Table.ColumnHeader>
                <Table.ColumnHeader textAlign="end">
                  Действия
                </Table.ColumnHeader>
              </Table.Row>
            </Table.Header>

            <Table.Body>
              {coordinatesStore.coordinates.map((c) => (
                <Table.Row key={c.id}>
                  <Table.Cell>{c.id}</Table.Cell>
                  <Table.Cell>{c.x}</Table.Cell>
                  <Table.Cell>{c.y}</Table.Cell>
                  <Table.Cell textAlign="end">
                    <Flex justify="flex-end" gap={2}>
                      <IconButton
                        aria-label="Просмотр"
                        size="xs"
                        variant="ghost"
                        onClick={() => navigate(`/coordinates/${c.id}`)}
                      >
                        <Eye size={16} />
                      </IconButton>
                      <IconButton
                        aria-label="Редактировать"
                        size="xs"
                        variant="ghost"
                        colorScheme="blue"
                        onClick={() =>
                          navigate(`/coordinates/${c.id}/edit`)
                        }
                      >
                        <Pencil size={16} />
                      </IconButton>
                      <IconButton
                        aria-label="Удалить"
                        size="xs"
                        variant="ghost"
                        colorScheme="red"
                        onClick={() => handleDelete(c.id)}
                      >
                        <Trash2 size={16} />
                      </IconButton>
                    </Flex>
                  </Table.Cell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table.Root>
        )}
      </Box>

      {totalItems > pageSize && (
        <Flex justify="center" mt={6}>
          <Pagination.Root
            count={totalItems}
            pageSize={pageSize}
            page={currentPage}
            onPageChange={handlePageChange}
          >
            <ButtonGroup variant="ghost" size="sm">
              <Pagination.PrevTrigger asChild>
                <IconButton aria-label="Предыдущая страница">
                  <ChevronLeft size={16} />
                </IconButton>
              </Pagination.PrevTrigger>

              <Pagination.Items
                render={(page) => (
                  <IconButton
                    key={page.value}
                    variant={{ base: 'ghost', _selected: 'outline' }}
                  >
                    {page.value}
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
    </Box>
  );
});

export default CoordinatesPage;

