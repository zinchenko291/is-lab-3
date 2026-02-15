import { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { useNavigate } from 'react-router';
import {
  Box,
  Button,
  FileUpload,
  Flex,
  Portal,
  Select,
  Spinner,
  Table,
  Text,
  createListCollection,
} from '@chakra-ui/react';

import { useStores } from '../../store';
import { toaster } from '../../components/ui/toaster';
import { getApiErrorMessage } from '../../api/getApiErrorMessage';
import type { ImportUploadFormat } from '../../api/models/imports';
import {
  importFormatLabels,
  importStatusLabels,
} from '../../api/models/imports';
import Date from '../../components/Date';

type FormatOption = ImportUploadFormat | 'auto';

const ImportsPage = observer(() => {
  const { importsStore } = useStores();
  const navigate = useNavigate();

  const maxFileSizeBytes = 2 * 1024 * 1024;
  const [file, setFile] = useState<File | null>(null);
  const [uploadKey, setUploadKey] = useState(0);
  const [format, setFormat] = useState<FormatOption>('auto');
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    importsStore.loadHistory();
  }, [importsStore]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!file) {
      setFormError('Выберите файл для импорта');
      return;
    }

    if (file.size > maxFileSizeBytes) {
      setFormError('Размер файла не должен превышать 2 МБ');
      return;
    }

    try {
      const selectedFormat = format === 'auto' ? undefined : format;
      await importsStore.startImport(file, selectedFormat);
      toaster.create({
        title: 'Импорт запущен',
        type: 'success',
      });
      setFile(null);
      setUploadKey((prev) => prev + 1);
    } catch (e: any) {
      const message = await getApiErrorMessage(e);
      setFormError(
        importsStore.startError ?? message ?? 'Не удалось запустить импорт'
      );
    }
  };

  return (
    <Box p={6}>
      <Flex justify="space-between" align="center" mb={6}>
        <Text fontSize="2xl" fontWeight="bold">
          Импорт
        </Text>
      </Flex>

      <Box
        as="form"
        onSubmit={handleSubmit}
        borderWidth="1px"
        borderRadius="md"
        p={4}
        mb={6}
      >
        <Flex direction="column" gap={4}>
          <Box>
            <Text mb={1} fontWeight="medium">
              Файл импорта
            </Text>
            <FileUpload.Root
              key={uploadKey}
              accept={['.yaml', '.yml', '.xml']}
              maxFiles={1}
              onFileAccept={(accepted) => {
                const selectedFile = accepted.files?.[0] ?? null;

                if (selectedFile && selectedFile.size > maxFileSizeBytes) {
                  setFormError('Размер файла не должен превышать 2 МБ');
                  setFile(null);
                  setUploadKey((prev) => prev + 1);
                  return;
                }

                setFile(selectedFile);
                setFormError(null);
              }}
            >
              <FileUpload.HiddenInput />
              <FileUpload.Trigger asChild>
                <Button size="sm" variant="outline">
                  Выбрать файл
                </Button>
              </FileUpload.Trigger>
              <FileUpload.List />
            </FileUpload.Root>
            {file && (
              <Text mt={2} fontSize="sm" color="gray.500">
                Выбран файл: {file.name}
              </Text>
            )}
          </Box>

          {(formError || importsStore.startError) && (
            <Text fontSize="sm" color="red.500">
              {formError ?? importsStore.startError}
            </Text>
          )}

          <Flex justify="flex-end">
            <Button
              colorScheme="teal"
              size="sm"
              type="submit"
              disabled={importsStore.startLoading}
              loading={importsStore.startLoading}
            >
              {importsStore.startLoading ? 'Запуск...' : 'Запустить импорт'}
            </Button>
          </Flex>
        </Flex>
      </Box>

      {importsStore.historyError && (
        <Text mb={3} fontSize="sm" color="red.500">
          {importsStore.historyError}
        </Text>
      )}

      <Box
        borderWidth="1px"
        borderRadius="md"
        overflowX="auto"
        position="relative"
        minH="220px"
      >
        {importsStore.historyLoading && (
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

        {!importsStore.historyLoading &&
          importsStore.operations.length === 0 && (
            <Flex justify="center" align="center" p={4} minH="220px">
              <Text>Импорты не найдены</Text>
            </Flex>
          )}

        {importsStore.operations.length > 0 && (
          <Table.Root size="sm" opacity={importsStore.historyLoading ? 0.6 : 1}>
            <Table.Header>
              <Table.Row>
                <Table.ColumnHeader>ID</Table.ColumnHeader>
                <Table.ColumnHeader>Статус</Table.ColumnHeader>
                <Table.ColumnHeader>Формат</Table.ColumnHeader>
                <Table.ColumnHeader>Запущен</Table.ColumnHeader>
                <Table.ColumnHeader>Завершён</Table.ColumnHeader>
                <Table.ColumnHeader>Добавлено</Table.ColumnHeader>
                <Table.ColumnHeader>Ошибка</Table.ColumnHeader>
                <Table.ColumnHeader>Пользователь</Table.ColumnHeader>
                <Table.ColumnHeader textAlign="end">
                  Действия
                </Table.ColumnHeader>
              </Table.Row>
            </Table.Header>
            <Table.Body>
              {importsStore.operations.map((op) => (
                <Table.Row key={op.id}>
                  <Table.Cell>{op.id}</Table.Cell>
                  <Table.Cell>{importStatusLabels[op.status]}</Table.Cell>
                  <Table.Cell>{importFormatLabels[op.format]}</Table.Cell>
                  <Table.Cell>
                    <Date>{op.startedAt}</Date>
                  </Table.Cell>
                  <Table.Cell>
                    {op.completedAt ? <Date>{op.completedAt}</Date> : '—'}
                  </Table.Cell>
                  <Table.Cell>{op.addedCount ?? '—'}</Table.Cell>
                  <Table.Cell>{op.errorMessage ?? '—'}</Table.Cell>
                  <Table.Cell>{op.user?.name ?? '—'}</Table.Cell>
                  <Table.Cell textAlign="end">
                    <Button
                      size="xs"
                      variant="outline"
                      onClick={() => navigate(`/imports/${op.id}/conflicts`)}
                    >
                      Конфликты
                    </Button>
                  </Table.Cell>
                </Table.Row>
              ))}
            </Table.Body>
          </Table.Root>
        )}
      </Box>
    </Box>
  );
});

export default ImportsPage;


