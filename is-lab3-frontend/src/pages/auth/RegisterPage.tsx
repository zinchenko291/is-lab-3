import {
  Box,
  Button,
  Field,
  Heading,
  Input,
  Stack,
  Text,
} from '@chakra-ui/react';
import { useState } from 'react';
import { Link } from 'react-router';
import * as ed25519 from '@noble/ed25519';
import { AuthService } from '../../api/services/authService.ts';
import {
  bytesToBase64,
  challengeToBytes,
  ensureEd25519,
  PRIVATE_KEY_STORAGE_KEY,
  rawPrivateKeyToPem,
  rawPublicKeyToDer,
} from '../../utils/ed25519KeyUtils.ts';
import { LuDownload } from 'react-icons/lu';

const RegisterPage = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [privateKeyPem, setPrivateKeyPem] = useState<string | null>(null);

  const handleRegister = async () => {
    setError(null);
    setPrivateKeyPem(null);
    const trimmedName = name.trim();
    if (!trimmedName) {
      setError('Введите имя.');
      return;
    }

    setLoading(true);
    try {
      ensureEd25519();
      const privateKeyRaw = ed25519.utils.randomSecretKey();
      const publicKeyRaw = ed25519.getPublicKey(privateKeyRaw);
      const publicKeyDer = rawPublicKeyToDer(publicKeyRaw);
      const publicKeyDerBase64 = bytesToBase64(publicKeyDer);

      const { challenge } = await AuthService.register({
        name: trimmedName,
        pubkey: publicKeyDerBase64,
        email: email.trim(),
      });
      const signature = ed25519.sign(
        challengeToBytes(challenge),
        privateKeyRaw,
      );
      await AuthService.registerVerify({
        pubkey: publicKeyDerBase64,
        signature: bytesToBase64(signature),
      });

      const pem = rawPrivateKeyToPem(privateKeyRaw);
      localStorage.setItem(PRIVATE_KEY_STORAGE_KEY, pem);
      setPrivateKeyPem(pem);
    } catch (err) {
      console.error(err);
      setError('Не удалось завершить регистрацию.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = () => {
    if (!privateKeyPem) return;
    const blob = new Blob([privateKeyPem], { type: 'application/x-pem-file' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'ed25519-private-key.pem';
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <Stack height="100dvh" alignItems="center" justifyContent="center">
      <Box
        width="min(460px, 90vw)"
        borderWidth={1}
        borderRadius={8}
        borderColor="border.emphasized"
        bg="bg.muted"
        p={6}
      >
        <Stack gap={4}>
          <Heading size="lg">Регистрация</Heading>
          <Field.Root>
            <Field.Label>
              Имя
              <Field.RequiredIndicator />
            </Field.Label>
            <Input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Введите имя"
              required
            />
          </Field.Root>
          <Field.Root>
            <Field.Label>
              Email
              <Field.RequiredIndicator />
            </Field.Label>
            <Input
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="Введите email"
              type={'email'}
              required
            />
          </Field.Root>
          {error && <Text color="red.400">{error}</Text>}
          {privateKeyPem ? (
            <Stack gap={3}>
              <Text>
                Регистрация завершена. Скачайте приватный ключ и сохраните его.
              </Text>
              <Button colorScheme="teal" onClick={handleDownload}>
                <LuDownload /> Скачать приватный ключ
              </Button>
              <Link to="/auth" style={{ textDecoration: 'underline' }}>
                Перейти к входу
              </Link>
            </Stack>
          ) : (
            <Stack gap={3}>
              <Button
                colorScheme="teal"
                onClick={handleRegister}
                loading={loading}
              >
                Зарегистрироваться
              </Button>
              <Link to="/auth" style={{ textDecoration: 'underline' }}>
                Войти
              </Link>
            </Stack>
          )}
        </Stack>
      </Box>
    </Stack>
  );
};

export default RegisterPage;
