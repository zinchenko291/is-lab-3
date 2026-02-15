import { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate, useParams } from 'react-router';
import { Box, Button, Flex, Spinner, Text } from '@chakra-ui/react';
import { ArrowLeft, Pencil, MapPin, Trash2 } from 'lucide-react';

import { useStores } from '../../store';
import Date from '../../components/Date.tsx';
import { toaster } from '../../components/ui/toaster.tsx';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const VehicleViewPage = observer(() => {
  const navigate = useNavigate();
  const params = useParams<{ id: string }>();
  const { vehiclesStore } = useStores();

  const id = Number(params.id);
  const isInvalidId = !params.id || Number.isNaN(id);

  const vehicle = !isInvalidId ? vehiclesStore.getById(id) : null;
  const { loading, error } = vehiclesStore;

  useEffect(() => {
    if (isInvalidId) return;

    if (!vehicle) {
      vehiclesStore.fetchById(id).catch(() => {});
    }
  }, [isInvalidId, id, vehicle, vehiclesStore]);

  const handleDelete = async () => {
    if (!vehicle) return;

    const confirmed = window.confirm(
      `Вы действительно хотите удалить транспортное средство #${vehicle.id}?`,
    );
    if (!confirmed) return;

    try {
      await vehiclesStore.deleteVehicle(vehicle.id);
      toaster.create({
        title: 'Транспортное средство было удалено',
        type: 'success',
      });
      navigate('/');
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      toaster.create({
        title: message ?? 'Возникла непредвиденная ошибка',
        type: 'error',
      });
    }
  };

  if (isInvalidId) {
    return (
      <Box p={6}>
        <Text>Некорректный идентификатор транспортного средства.</Text>
        <Button mt={4} onClick={() => navigate('/')}>
          <Flex align="center" gap={2}>
            <ArrowLeft size={16} />
            <Text>Назад к списку</Text>
          </Flex>
        </Button>
      </Box>
    );
  }

  if (loading && !vehicle) {
    return (
      <Box p={6}>
        <Flex align="center" gap={3}>
          <Spinner size="sm" />
          <Text>Загрузка транспортного средства #{id}...</Text>
        </Flex>
      </Box>
    );
  }

  if (error && !vehicle) {
    return (
      <Box p={6}>
        <Flex justify="space-between" align="center" mb={4}>
          <Text fontSize="2xl" fontWeight="bold">
            Транспортное средство #{id}
          </Text>
          <Button onClick={() => navigate('/')}>
            <Flex align="center" gap={2}>
              <ArrowLeft size={16} />
              <Text>Назад</Text>
            </Flex>
          </Button>
        </Flex>

        <Text color="red.500">{error}</Text>
      </Box>
    );
  }

  if (!vehicle) {
    return (
      <Box p={6}>
        <Flex justify="space-between" align="center" mb={4}>
          <Text fontSize="2xl" fontWeight="bold">
            Транспортное средство #{id}
          </Text>
          <Button onClick={() => navigate('/')}>
            <Flex align="center" gap={2}>
              <ArrowLeft size={16} />
              <Text>Назад</Text>
            </Flex>
          </Button>
        </Flex>

        <Text>Транспортное средство не найдено.</Text>
      </Box>
    );
  }

  const coords = vehicle.coordinates;

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Транспортное средство #{vehicle.id}
        </Text>

        <Flex gap={3}>
          <Button variant="outline" onClick={() => navigate('/')}>
            <Flex align="center" gap={2}>
              <ArrowLeft size={16} />
              <Text>Назад</Text>
            </Flex>
          </Button>

          <Button
            colorScheme="blue"
            onClick={() => navigate(`/vehicles/${vehicle.id}/edit`)}
          >
            <Flex align="center" gap={2}>
              <Pencil size={16} />
              <Text>Изменить</Text>
            </Flex>
          </Button>

          <Button colorScheme="red" variant="outline" onClick={handleDelete}>
            <Flex align="center" gap={2}>
              <Trash2 size={16} />
              <Text>Удалить</Text>
            </Flex>
          </Button>
        </Flex>
      </Flex>

      <Flex gap={6} flexWrap="wrap" align="flex-start">
        <Box borderWidth="1px" borderRadius="md" p={4} minW="320px">
          <Text fontSize="lg" fontWeight="semibold" mb={3}>
            Общая информация
          </Text>

          <Flex direction="column" gap={3}>
            <Box>
              <Text fontSize="sm" color="gray.500">
                ID
              </Text>
              <Text fontSize="lg">{vehicle.id}</Text>
            </Box>

            <Box>
              <Text fontSize="sm" color="gray.500">
                Название
              </Text>
              <Text fontSize="lg">{vehicle.name}</Text>
            </Box>

            <Box>
              <Text fontSize="sm" color="gray.500">
                Тип ТС
              </Text>
              <Text fontSize="lg">{vehicle.type ?? '—'}</Text>
            </Box>

            <Box>
              <Text fontSize="sm" color="gray.500">
                Дата создания
              </Text>
              <Text fontSize="lg">
                <Date>{vehicle.creationDate}</Date>
              </Text>
            </Box>

            <Flex gap={4} flexWrap="wrap">
              <Box>
                <Text fontSize="sm" color="gray.500">
                  Мощность двигателя
                </Text>
                <Text fontSize="lg">{vehicle.enginePower}</Text>
              </Box>

              <Box>
                <Text fontSize="sm" color="gray.500">
                  Количество колёс
                </Text>
                <Text fontSize="lg">{vehicle.numberOfWheels}</Text>
              </Box>
            </Flex>

            <Flex gap={4} flexWrap="wrap">
              <Box>
                <Text fontSize="sm" color="gray.500">
                  Пробег
                </Text>
                <Text fontSize="lg">{vehicle.distanceTravelled}</Text>
              </Box>
            </Flex>

            <Flex gap={4} flexWrap="wrap">
              <Box>
                <Text fontSize="sm" color="gray.500">
                  Тип топлива
                </Text>
                <Text fontSize="lg">{vehicle.fuelType ?? '—'}</Text>
              </Box>
            </Flex>

            <Flex gap={4} flexWrap="wrap">
              <Box>
                <Text fontSize="sm" color="gray.500">
                  Владелец
                </Text>
                <Text fontSize="lg">{vehicle.owner.name ?? '—'}</Text>
              </Box>
            </Flex>
          </Flex>
        </Box>

        <Box borderWidth="1px" borderRadius="md" p={4} minW="280px">
          <Flex align="center" justify="space-between" mb={3}>
            <Flex align="center" gap={2}>
              <MapPin size={18} />
              <Text fontSize="lg" fontWeight="semibold">
                Координаты
              </Text>
            </Flex>
            {coords && (
              <Button
                size="sm"
                variant="outline"
                onClick={() => navigate(`/coordinates/${coords.id}`)}
              >
                <Text fontSize="sm">Открыть координату</Text>
              </Button>
            )}
          </Flex>

          {coords ? (
            <Flex direction="column" gap={3}>
              <Box>
                <Text fontSize="sm" color="gray.500">
                  ID координаты
                </Text>
                <Text fontSize="lg">{coords.id}</Text>
              </Box>

              <Flex gap={4}>
                <Box>
                  <Text fontSize="sm" color="gray.500">
                    X
                  </Text>
                  <Text fontSize="lg">{coords.x}</Text>
                </Box>
                <Box>
                  <Text fontSize="sm" color="gray.500">
                    Y
                  </Text>
                  <Text fontSize="lg">{coords.y}</Text>
                </Box>
              </Flex>
            </Flex>
          ) : (
            <Text fontSize="sm" color="gray.500">
              Координаты не заданы.
            </Text>
          )}
        </Box>
      </Flex>
    </Box>
  );
});

export default VehicleViewPage;

