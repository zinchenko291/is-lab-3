import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router';
import { observer } from 'mobx-react-lite';
import { Box, Button, Flex, Spinner, Text } from '@chakra-ui/react';
import { ArrowLeft, Pencil, Trash2 } from 'lucide-react';

import { useStores } from '../../store';
import { toaster } from '../../components/ui/toaster.tsx';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

const CoordinateViewPage = observer(() => {
  const navigate = useNavigate();
  const params = useParams<{ id: string }>();
  const { coordinatesStore } = useStores();

  const id = Number(params.id);
  const isInvalidId = !params.id || Number.isNaN(id);

  const coordinate = !isInvalidId ? coordinatesStore.getById(id) : null;
  const { loading, error } = coordinatesStore;

  const handleDelete = async () => {
    if (!coordinate) return;

    const confirmed = window.confirm(
      `Вы действительно хотите удалить координату #${coordinate.id}?`,
    );
    if (!confirmed) return;

    try {
      await coordinatesStore.deleteCoordinates(coordinate.id);
      toaster.create({
        title: 'Координата была удалена',
        type: 'success',
      });
      navigate(-1);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      toaster.create({
        title: message ?? 'Возникла непредвиденная ошибка',
        type: 'error',
      });
    }
  }

  useEffect(() => {
    if (isInvalidId) return;
    if (!coordinate) {
      coordinatesStore.fetchById(id).catch(() => {});
    }
  }, [isInvalidId, id, coordinate, coordinatesStore]);

  if (isInvalidId) {
    return (
      <Box p={6}>
        <Text>Некорректный идентификатор координаты.</Text>
        <Button mt={4} onClick={() => navigate(-1)}>
          <Flex align="center" gap={2}>
            <ArrowLeft size={16} />
            <Text>Назад</Text>
          </Flex>
        </Button>
      </Box>
    );
  }

  if (loading && !coordinate) {
    return (
      <Box p={6}>
        <Flex align="center" gap={3}>
          <Spinner size="sm" />
          <Text>Загрузка координаты #{id}...</Text>
        </Flex>
      </Box>
    );
  }

  if (error && !coordinate) {
    return (
      <Box p={6}>
        <Flex justify="space-between" align="center" mb={4}>
          <Text fontSize="2xl" fontWeight="bold">
            Координата #{id}
          </Text>
          <Button onClick={() => navigate(-1)}>
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

  if (!coordinate) {
    return (
      <Box p={6}>
        <Flex justify="space-between" align="center" mb={4}>
          <Text fontSize="2xl" fontWeight="bold">
            Координата #{id}
          </Text>
          <Button onClick={() => navigate(-1)}>
            <Flex align="center" gap={2}>
              <ArrowLeft size={16} />
              <Text>Назад</Text>
            </Flex>
          </Button>
        </Flex>

        <Text>Координата не найдена.</Text>
      </Box>
    );
  }

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Координата #{coordinate.id}
        </Text>

        <Flex gap={3}>
          <Button variant="outline" onClick={() => navigate(-1)}>
            <Flex align="center" gap={2}>
              <ArrowLeft size={16} />
              <Text>Назад</Text>
            </Flex>
          </Button>

          <Button
            colorScheme="blue"
            onClick={() => navigate(`/coordinates/${coordinate.id}/edit`)}
          >
            <Flex align="center" gap={2}>
              <Pencil size={16} />
              <Text>Изменить</Text>
            </Flex>
          </Button>
          
          <Button
            colorScheme="red"
            variant="outline"
            onClick={handleDelete}
          >
            <Flex align="center" gap={2}>
              <Trash2 size={16} />
              <Text>Удалить</Text>
            </Flex>
          </Button>          
        </Flex>
      </Flex>

      <Box borderWidth="1px" borderRadius="md" p={4} maxW="400px">
        <Flex direction="column" gap={3}>
          <Box>
            <Text fontSize="sm" color="gray.500">
              ID
            </Text>
            <Text fontSize="lg">{coordinate.id}</Text>
          </Box>

          <Box>
            <Text fontSize="sm" color="gray.500">
              X
            </Text>
            <Text fontSize="lg">{coordinate.x}</Text>
          </Box>

          <Box>
            <Text fontSize="sm" color="gray.500">
              Y
            </Text>
            <Text fontSize="lg">{coordinate.y}</Text>
          </Box>
        </Flex>
      </Box>
    </Box>
  );
});

export default CoordinateViewPage;

