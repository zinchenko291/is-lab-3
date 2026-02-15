import {
  Accordion,
  Box,
  Button,
  Field,
  FileUpload,
  Heading,
  Stack,
  Text,
  Textarea,
} from '@chakra-ui/react';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import * as ed25519 from '@noble/ed25519';
import { AuthService } from '../../api/services/authService.ts';
import {
  bytesToBase64,
  challengeToBytes,
  ensureEd25519,
  PRIVATE_KEY_STORAGE_KEY,
  privateKeyPemToRaw,
  rawPublicKeyToDer,
} from '../../utils/ed25519KeyUtils.ts';

const SignInPage = () => {
  const navigate = useNavigate();
  const [savedKey, setSavedKey] = useState<string | null>(null);
  const [pemInput, setPemInput] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(PRIVATE_KEY_STORAGE_KEY);
    if (stored) {
      setSavedKey(stored);
    }
  }, []);

  const handleFile = async (file?: File) => {
    if (!file) return;
    setError(null);
    const text = await file.text();
    setPemInput(text);
  };

  const handleLogin = async (pem: string) => {
    setError(null);
    if (!pem.trim()) {
      setError('Введите приватный ключ.');
      return;
    }

    setLoading(true);
    try {
      ensureEd25519();
      const privateKeyRaw = privateKeyPemToRaw(pem);
      const publicKeyRaw = ed25519.getPublicKey(privateKeyRaw);
      const publicKeyDer = rawPublicKeyToDer(publicKeyRaw);
      const publicKeyDerBase64 = bytesToBase64(publicKeyDer);

      const { challenge } = await AuthService.login({
        pubkey: publicKeyDerBase64,
      });
      const signature = ed25519.sign(
        challengeToBytes(challenge),
        privateKeyRaw,
      );
      await AuthService.loginVerify({
        pubkey: publicKeyDerBase64,
        signature: bytesToBase64(signature),
      });

      localStorage.setItem(PRIVATE_KEY_STORAGE_KEY, pem);
      setSavedKey(pem);
      navigate('/');
    } catch (err) {
      console.error(err);
      setError('Не удалось выполнить вход.');
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveSaved = () => {
    localStorage.removeItem(PRIVATE_KEY_STORAGE_KEY);
    setSavedKey(null);
  };

  return (
    <Stack height="100dvh" alignItems="center" justifyContent="center">
      <Box
        width="min(520px, 92vw)"
        borderWidth={1}
        borderRadius={8}
        borderColor="border.muted"
        bg="bg.muted"
        p={6}
      >
        <Stack gap={4}>
          <Heading size="lg">Вход</Heading>
          {savedKey ? (
            <Box borderWidth={1} borderRadius={6} p={4}>
              <Stack gap={3}>
                <Button
                  colorScheme="teal"
                  onClick={() => handleLogin(savedKey)}
                  loading={loading}
                >
                  Войти с сохраненным ключом
                </Button>
                <Button variant={'outline'} onClick={handleRemoveSaved}>
                  Удалить ключ из сохранённых
                </Button>
              </Stack>
            </Box>
          ) : (
            <>
              <Accordion.Root defaultValue={['file']}>
                <Accordion.Item value={'file'}>
                  <Accordion.ItemTrigger>
                    <Accordion.ItemIndicator />
                    <Text>Войти по файлу</Text>
                  </Accordion.ItemTrigger>
                  <Accordion.ItemContent>
                    <Accordion.ItemBody>
                      <Field.Root>
                        <Field.Label>
                          Загрузить приватный ключ (PEM)
                        </Field.Label>
                        <FileUpload.Root
                          maxFiles={1}
                          accept={[
                            '.pem',
                            'application/x-pem-file',
                            'text/plain',
                          ]}
                          onFileAccept={(accepted) => {
                            const file = accepted.files?.[0];
                            if (file) {
                              void handleFile(file);
                            }
                          }}
                        >
                          <FileUpload.HiddenInput />
                          <FileUpload.Trigger asChild>
                            <Button variant="outline" size="sm">
                              Выбрать файл с ключом
                            </Button>
                          </FileUpload.Trigger>
                          <FileUpload.List />
                        </FileUpload.Root>
                      </Field.Root>
                    </Accordion.ItemBody>
                  </Accordion.ItemContent>
                </Accordion.Item>
                <Accordion.Item value={'text'}>
                  <Accordion.ItemTrigger>
                    <Accordion.ItemIndicator />
                    <Text>Войти по вводу тексту</Text>
                  </Accordion.ItemTrigger>
                  <Accordion.ItemContent>
                    <Accordion.ItemBody>
                      <Field.Root>
                        <Field.Label>Или вставьте приватный ключ</Field.Label>
                        <Textarea
                          value={pemInput}
                          onChange={(event) => setPemInput(event.target.value)}
                          placeholder="-----BEGIN PRIVATE KEY-----"
                          rows={6}
                        />
                      </Field.Root>
                      {error && <Text color="red.400">{error}</Text>}
                    </Accordion.ItemBody>
                  </Accordion.ItemContent>
                </Accordion.Item>
              </Accordion.Root>
              <Button
                colorScheme="teal"
                onClick={() => handleLogin(pemInput)}
                loading={loading}
              >
                Войти
              </Button>
              <Link to="/auth/register" style={{ textDecoration: 'underline' }}>
                Зарегистрироваться
              </Link>
            </>
          )}
        </Stack>
      </Box>
    </Stack>
  );
};

export default SignInPage;




