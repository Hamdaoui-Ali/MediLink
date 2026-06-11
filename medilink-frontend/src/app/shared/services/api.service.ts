import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly http = inject(HttpClient);

  get<T>(path: string): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(path);
  }

  post<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.post<ApiResponse<T>>(path, body);
  }

  put<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.put<ApiResponse<T>>(path, body);
  }

  patch<T>(path: string, body: unknown): Observable<ApiResponse<T>> {
    return this.http.patch<ApiResponse<T>>(path, body);
  }
}
