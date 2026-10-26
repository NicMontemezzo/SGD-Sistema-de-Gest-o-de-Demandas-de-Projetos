/** Formato único de erro devolvido pela API (ErrorResponse do back-end). */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details: string[];
}
