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

import { useStores } from '../store';
import VehiclesFilter from '../components/VehiclesFilter';
import { toaster } from '../components/ui/toaster';
import { getApiErrorMessage } from '../api/getApiErrorMessage';

const MainPage = observer(() => {
  const { vehiclesStore } = useStores();
  const navigate = useNavigate();

  useEffect(() => {
    vehiclesStore.loadVehicles(0);
  }, [vehiclesStore]);

  const handlePageChange = (details: any) => {
    const nextPage = details.page as number;
    vehiclesStore.goToPage(nextPage - 1);
  };

  const handleDelete = async (id: number) => {
    const confirmed = window.confirm(
      `Вы действительно хотите удалить транспортное средство #${id}?`,
    );
    if (!confirmed) return;

    try {
      await vehiclesStore.deleteVehicle(id);
      toaster.create({
        title: 'Транспортное средство было удалено',
        type: 'success',
      });
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      toaster.create({
        title: message ?? 'Возникла непредвиденная ошибка',
        type: 'error',
      });
    }
  };

  const totalItems = vehiclesStore.total;
  const pageSize = vehiclesStore.pageSize;
  const currentPage = vehiclesStore.currentPage + 1;

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Транспортные средства
        </Text>
        <Button colorScheme="teal" onClick={() => navigate('/vehicles/create')}>
          Создать ТС
        </Button>
      </Flex>

      <VehiclesFilter
        initialField={vehiclesStore.filters.field}
        initialValue={vehiclesStore.filters.value}
        initialOrderBy={vehiclesStore.filters.orderBy}
      />

      <Box
        borderWidth="1px"
        borderRadius="md"
        overflowX="auto"
        position="relative"
        minH="220px"
      >
        {vehiclesStore.loading && (
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

        {!vehiclesStore.loading && vehiclesStore.vehicles.length === 0 && (
          <Flex justify="center" align="center" p={4} minH="220px">
            <Text>Объекты не найдены</Text>
          </Flex>
        )}

        {vehiclesStore.vehicles.length > 0 && (
          <Table.Root size="sm" opacity={vehiclesStore.loading ? 0.6 : 1}>
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>Название</Table.ColumnHeader>
                <Table.ColumnHeader>Коорд. X</Table.ColumnHeader>
                <Table.ColumnHeader>Коорд. Y</Table.ColumnHeader>
                <Table.ColumnHeader>Мощность</Table.ColumnHeader>
                <Table.ColumnHeader>Колёс</Table.ColumnHeader>
                <Table.ColumnHeader>Пробег</Table.ColumnHeader>
                <Table.ColumnHeader>Тип</Table.ColumnHeader>
                <Table.ColumnHeader>Топливо</Table.ColumnHeader>
                <Table.ColumnHeader textAlign="end">
                  Действия
                </Table.ColumnHeader>
              </Table.Row>
            </Table.Header>

            <Table.Body>
              {vehiclesStore.vehicles.map((v) => (
                <Table.Row key={v.id}>
                  <Table.Cell>{v.id}</Table.Cell>
                  <Table.Cell>{v.name}</Table.Cell>
                  <Table.Cell>{v.coordinates.x}</Table.Cell>
                  <Table.Cell>{v.coordinates.y}</Table.Cell>
                  <Table.Cell>{v.enginePower}</Table.Cell>
                  <Table.Cell>{v.numberOfWheels}</Table.Cell>
                  <Table.Cell>{v.distanceTravelled}</Table.Cell>
                  <Table.Cell>{v.type ?? '—'}</Table.Cell>
                  <Table.Cell>{v.fuelType ?? '—'}</Table.Cell>
                  <Table.Cell textAlign="end">
                    <Flex justify="flex-end" gap={2}>
                      <IconButton
                        aria-label="Просмотр"
                        size="xs"
                        variant="ghost"
                        onClick={() => navigate(`/vehicles/${v.id}`)}
                      >
                        <Eye size={16} />
                      </IconButton>
                      <IconButton
                        aria-label="Редактировать"
                        size="xs"
                        variant="ghost"
                        colorScheme="blue"
                        onClick={() => navigate(`/vehicles/${v.id}/edit`)}
                      >
                        <Pencil size={16} />
                      </IconButton>
                      <IconButton
                        aria-label="Удалить"
                        size="xs"
                        variant="ghost"
                        colorScheme="red"
                        onClick={() => handleDelete(v.id)}
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

export default MainPage;

