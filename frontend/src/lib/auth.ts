import { apiClient } from "./api-client";

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  employee: {
    id: number;
    name: string;
    email: string;
    role: string;
  };
}

export async function login(
  email: string,
  password: string
): Promise<LoginResponse> {
  const response = await apiClient<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });

  localStorage.setItem("accessToken", response.accessToken);
  localStorage.setItem("refreshToken", response.refreshToken);
  localStorage.setItem("employee", JSON.stringify(response.employee));

  return response;
}

export function logout() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("employee");
}

export function getEmployee(): LoginResponse["employee"] | null {
  if (typeof window === "undefined") return null;
  const stored = localStorage.getItem("employee");
  if (!stored) return null;
  return JSON.parse(stored);
}

export function isAuthenticated(): boolean {
  if (typeof window === "undefined") return false;
  return !!localStorage.getItem("accessToken");
}
