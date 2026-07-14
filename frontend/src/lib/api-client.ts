const BASE_PATH = process.env.NEXT_PUBLIC_BASE_PATH || "";
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "";

interface ApiError {
  error: string;
  message: string;
  details: { field: string; message: string }[];
}

export class ApiClientError extends Error {
  constructor(
    public status: number,
    public body: ApiError
  ) {
    super(body.message);
  }
}

export async function apiClient<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (token) {
    (headers as Record<string, string>)["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_PATH}${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const body = await response.json().catch(() => ({
      error: "UNKNOWN",
      message: "通信エラーが発生しました",
      details: [],
    }));
    throw new ApiClientError(response.status, body);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}
