import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpHeaders,
} from '@angular/common/http';
import {
  Observable,
  BehaviorSubject,
  throwError,
  tap,
  catchError,
} from 'rxjs';

// Interfaces for request/response types
export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  clg_name: string;
  phone_no: string;
}

export interface SignupResponse {
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role: string[];
  message: string;
  username: string;
}

export interface DashboardResponse {
  roles: string[];
  email: string;
  message: string;
  username: string;
  clg_name: string; 
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'sports_jwt_token';
  private authState = new BehaviorSubject<boolean>(false);
  private userData = new BehaviorSubject<{ email: string; username: string } | null>(null);

  constructor(private http: HttpClient) {
    // Initialize auth state from localStorage
    this.authState.next(this.isLoggedIn());
    if (this.isLoggedIn()) {
      this.getDashboard().subscribe();
    }
  }

  // Get auth state as observable
  get isAuthenticated$() {
    return this.authState.asObservable();
  }

  // Get user data as observable
  get userData$() {
    return this.userData.asObservable();
  }

  // Signup new user
  signup(userData: SignupRequest): Observable<SignupResponse> {
    return this.http
      .post<SignupResponse>(`${this.apiUrl}/signup`, userData)
      .pipe(catchError(this.handleError));
  }

  // Login user
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap((response) => {
          this.saveToken(response.token);
          this.authState.next(true);
          this.userData.next({
            email: credentials.email,
            username: response.username,
          });
        }),
        catchError(this.handleError)
      );
  }

  // Get dashboard data
  getDashboard(): Observable<DashboardResponse> {
    const headers = this.getAuthHeaders();
    return this.http
      .get<DashboardResponse>(`${this.apiUrl}/dashboard`, { headers })
      .pipe(
        tap((response) => {
          this.userData.next({
            email: response.email,
            username: response.username,
          });
        }),
        catchError(this.handleError)
      );
  }

  // Logout user
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.authState.next(false);
    this.userData.next(null);
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  // Get current JWT token
  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  // Private helper methods
  private saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage = error.error?.message || `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
