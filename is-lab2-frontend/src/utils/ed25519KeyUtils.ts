import * as ed25519 from '@noble/ed25519';
import { sha512 } from '@noble/hashes/sha2.js';
import { AsnParser, AsnSerializer } from '@peculiar/asn1-schema';
import { PrivateKey, PrivateKeyInfo } from '@peculiar/asn1-pkcs8';
import { AlgorithmIdentifier, SubjectPublicKeyInfo } from '@peculiar/asn1-x509';
import { base64 } from '@scure/base';

const ED25519_OID = '1.3.101.112';

export const PRIVATE_KEY_STORAGE_KEY = 'auth.privateKeyPem';

const bytesToArrayBuffer = (bytes: Uint8Array): ArrayBuffer =>
  Uint8Array.from(bytes).buffer;

export const bytesToBase64 = (bytes: Uint8Array): string =>
  base64.encode(bytes);

export const base64ToBytes = (value: string): Uint8Array =>
  base64.decode(value);

export const derToPem = (der: Uint8Array, label: string): string => {
  const encoded = bytesToBase64(der);
  const lines: string[] = [];
  for (let i = 0; i < encoded.length; i += 64) {
    lines.push(encoded.slice(i, i + 64));
  }
  return `-----BEGIN ${label}-----\n${lines.join('\n')}\n-----END ${label}-----`;
};

export const pemToDer = (pem: string): Uint8Array => {
  const normalized = pem
    .replace(/-----BEGIN [^-]+-----/g, '')
    .replace(/-----END [^-]+-----/g, '')
    .replace(/\s+/g, '');
  if (!normalized) {
    throw new Error('PEM is empty.');
  }
  return base64ToBytes(normalized);
};

export const privateKeyPemToRaw = (pem: string): Uint8Array => {
  const der = pemToDer(pem);
  const keyInfo = AsnParser.parse(bytesToArrayBuffer(der), PrivateKeyInfo);
  const privateKey = new Uint8Array(keyInfo.privateKey.buffer);
  if (privateKey.length === 32) {
    return privateKey;
  }
  if (privateKey.length === 34 && privateKey[0] === 0x04 && privateKey[1] === 0x20) {
    return privateKey.slice(2);
  }
  throw new Error('Unsupported Ed25519 private key format.');
};

export const rawPrivateKeyToPem = (raw: Uint8Array): string => {
  if (raw.length !== 32) {
    throw new Error('Private key must be 32 bytes.');
  }
  const keyInfo = new PrivateKeyInfo({
    version: 0,
    privateKeyAlgorithm: new AlgorithmIdentifier({ algorithm: ED25519_OID }),
    privateKey: new PrivateKey(raw),
  });
  const der = new Uint8Array(AsnSerializer.serialize(keyInfo));
  return derToPem(der, 'PRIVATE KEY');
};

export const rawPublicKeyToDer = (raw: Uint8Array): Uint8Array => {
  if (raw.length !== 32) {
    throw new Error('Public key must be 32 bytes.');
  }
  const publicKeyInfo = new SubjectPublicKeyInfo({
    algorithm: new AlgorithmIdentifier({ algorithm: ED25519_OID }),
    subjectPublicKey: bytesToArrayBuffer(raw),
  });
  return new Uint8Array(AsnSerializer.serialize(publicKeyInfo));
};

const tryDecodeBase64 = (value: string): Uint8Array | null => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded =
    normalized + '='.repeat((4 - (normalized.length % 4)) % 4);
  try {
    const bytes = base64ToBytes(padded);
    const reencoded = bytesToBase64(bytes).replace(/=+$/g, '');
    if (reencoded === normalized.replace(/=+$/g, '')) {
      return bytes;
    }
  } catch {
    return null;
  }
  return null;
};

export const challengeToBytes = (challenge: string): Uint8Array => {
  const decoded = tryDecodeBase64(challenge);
  if (decoded) {
    return decoded;
  }
  return new TextEncoder().encode(challenge);
};

let hashesReady = false;

export const ensureEd25519 = () => {
  if (hashesReady) return;
  if (!ed25519.hashes?.sha512) {
    ed25519.hashes.sha512 = sha512;
  }
  hashesReady = true;
};
