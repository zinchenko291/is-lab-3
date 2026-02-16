import axios from 'axios';

const isBlob = (value: unknown): value is Blob =>
  typeof Blob !== 'undefined' && value instanceof Blob;

export const getApiErrorMessage = async (
  error: unknown
): Promise<string | null> => {
  if (!error) return null;

  if (axios.isAxiosError(error)) {
    const data = error.response?.data;

    if (typeof data === 'string' && data.trim().length > 0) {
      return data;
    }

    if (isBlob(data)) {
      try {
        const text = await data.text();
        if (text.trim().length > 0) return text;
      } catch {
        return null;
      }
    }

    if (
      data &&
      typeof data === 'object' &&
      'message' in data &&
      typeof (data as { message?: unknown }).message === 'string'
    ) {
      return (data as { message: string }).message;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return null;
};
