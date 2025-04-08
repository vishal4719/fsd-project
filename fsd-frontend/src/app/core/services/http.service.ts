import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

interface HttpOptions {
  headers?: HttpHeaders;
  params?: HttpParams;
  reportProgress?: boolean;
  withCredentials?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class HttpService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  get<T>(endpoint: string, options?: HttpOptions): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}/${endpoint}`, options);
  }

  post<T>(endpoint: string, data: any, options?: HttpOptions): Observable<T> {
    return this.http.post<T>(`${this.apiUrl}/${endpoint}`, data, options);
  }

  put<T>(endpoint: string, data: any, options?: HttpOptions): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}/${endpoint}`, data, options);
  }

  delete<T>(endpoint: string, options?: HttpOptions): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}/${endpoint}`, options);
  }
}
