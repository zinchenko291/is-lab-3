import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate, useParams } from 'react-router';
import { Box, Button, Flex, Input, Spinner, Text } from '@chakra-ui/react';
import { ArrowLeft, Save } from 'lucide-react';

import { useStores } from '../../store';
import validateCoordinatesForm from '../../api/validators/validateCoordinatesForm.ts';
import { toaster } from '../../components/ui/toaster.tsx';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

type FormState = {
  x: string;
  y: string;
};

type FormErrors = {
  x?: string;
  y?: string;
  common?: string;
};

const CoordinateEditPage = observer(() => {
  const { coordinatesStore } = useStores();
  const navigate = useNavigate();
  const params = useParams<{ id: string }>();

  const id = Number(params.id);
  const isInvalidId = !params.id || Number.isNaN(id);

  const coordinate = !isInvalidId ? coordinatesStore.getById(id) : null;
  const { loading, error } = coordinatesStore;

  const [form, setForm] = useState<FormState>({ x: '', y: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isInvalidId) return;

    if (!coordinate) {
      coordinatesStore.fetchById(id).catch(() => {});
    }
  }, [isInvalidId, id, coordinate, coordinatesStore]);

  useEffect(() => {
    if (coordinate) {
      setForm({
        x: String(coordinate.x),
        y: String(coordinate.y),
      });
    }
  }, [coordinate]);

  if (isInvalidId) {
    return (
      <Box p={6}>
        <Text>Некорректный идентификатор координаты.</Text>
        <Button mt={4} onClick={() => navigate(-1)}>
          Назад к списку
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
            Редактирование координаты #{id}
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
            Редактирование координаты #{id}
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

  const handleChange =
    (field: keyof FormState) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm((prev) => ({ ...prev, [field]: e.target.value }));
      setErrors((prev) => ({ ...prev, [field]: undefined, common: undefined }));
    };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const { dto, errors: validationErrors } = validateCoordinatesForm(form);

    if (validationErrors) {
      setErrors(validationErrors);
      return;
    }

    if (!dto) return;

    try {
      setSubmitting(true);
      await coordinatesStore.updateCoordinates(coordinate.id, { ...dto, id });
      toaster.create({
        title: 'Координата была обновлена',
        type: 'success'
      });
      navigate(`/coordinates/${coordinate.id}`);
    } catch (err: any) {
      const message = await getApiErrorMessage(err);
      setErrors({
        common: message ?? 'Не удалось сохранить координату',
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Редактирование координаты #{coordinate.id}
        </Text>

        <Flex gap={3}>
          <Button variant="outline" onClick={() => navigate(-1)}>
            <Flex align="center" gap={2}>
              <ArrowLeft size={16} />
              <Text>Отмена</Text>
            </Flex>
          </Button>
        </Flex>
      </Flex>

      <Box
        as="form"
        onSubmit={handleSubmit}
        borderWidth="1px"
        borderRadius="md"
        p={4}
        maxW="400px"
      >
        <Flex direction="column" gap={4}>
          <Box>
            <Text mb={1} fontWeight="medium">
              X
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.x}
              onChange={handleChange('x')}
              placeholder="Введите X"
            />
            {errors.x && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.x}
              </Text>
            )}
          </Box>

          <Box>
            <Text mb={1} fontWeight="medium">
              Y (≤ 910)
            </Text>
            <Input
              size="sm"
              type="number"
              value={form.y}
              onChange={handleChange('y')}
              placeholder="Введите Y"
            />
            {errors.y && (
              <Text mt={1} fontSize="sm" color="red.500">
                {errors.y}
              </Text>
            )}
          </Box>

          {errors.common && (
            <Text fontSize="sm" color="red.500">
              {errors.common}
            </Text>
          )}

          <Flex justify="flex-end" gap={3} mt={2}>
            <Button
              colorScheme="teal"
              size="sm"
              type="submit"
              disabled={submitting}
            >
              <Flex align="center" gap={2}>
                <Save size={16} />
                <Text>{submitting ? 'Сохранение...' : 'Сохранить'}</Text>
              </Flex>
            </Button>
          </Flex>
        </Flex>
      </Box>
    </Box>
  );
});

export default CoordinateEditPage;


