import React, { useState } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate } from 'react-router';
import {
  Box,
  Button,
  Center,
  Flex,
  Heading,
  Input,
  Text,
} from '@chakra-ui/react';
import { ArrowLeft } from 'lucide-react';

import { useStores } from '../../store';
import { toaster } from '../../components/ui/toaster.tsx';
import validateCoordinatesForm from '../../api/validators/validateCoordinatesForm.ts';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';

export type FormState = {
  x: string;
  y: string;
};

export type FormErrors = {
  x?: string;
  y?: string;
  common?: string;
};

const CoordinateCreatePage = observer(() => {
  const { coordinatesStore } = useStores();
  const navigate = useNavigate();

  const [form, setForm] = useState<FormState>({ x: '', y: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitting, setSubmitting] = useState(false);

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
      await coordinatesStore.createCoordinates(dto);
      toaster.create({
        title: 'Координата была создана',
        type: 'success',
      });
      navigate('/coordinates');
    } catch (err: any) {
      const message = await getApiErrorMessage(err);
      setErrors({
        common: message ?? 'Не удалось создать координаты',
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Heading size="lg">Создание координаты</Heading>
        <Button variant="ghost" onClick={() => navigate(-1)}>
          <Flex align="center" gap={2}>
            <ArrowLeft size={16} />
            <Text>Назад к списку</Text>
          </Flex>
        </Button>
      </Flex>

      <Center>
        <Box
          as="form"
          onSubmit={handleSubmit}
          borderWidth="1px"
          borderRadius="md"
          p={4}
          minW="400px"
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
                Y (Поле должно быть меньше 910)
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
                {submitting ? 'Сохранение...' : 'Сохранить'}
              </Button>
            </Flex>
          </Flex>
        </Box>
      </Center>
    </Box>
  );
});

export default CoordinateCreatePage;


