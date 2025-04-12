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
import { Router } from '@angular/router';

// Interfaces for request/response types
export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  clg_name: string;
  phone_no: string;
  roles: string;
}

export interface SignupResponse {
  message: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  message: string;
  token: string;
  role: string;
}

export interface DashboardResponse {
  message: string;
  username: string;
  role: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api';
  private tokenKey = 'auth_token';
  private userRoleKey = 'user_role';
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  private authState = new BehaviorSubject<boolean>(false);
  private userData = new BehaviorSubject<{ email: string; username: string } | null>(null);

  constructor(private http: HttpClient, private router: Router) {
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
  signup(data: SignupRequest): Observable<SignupResponse> {
    console.log('Sending signup request:', data);
    return this.http.post<SignupResponse>(`${this.apiUrl}/auth/signup`, data, { headers: this.getHeaders() })
      .pipe(
        tap(response => console.log('Signup response:', response)),
        catchError(this.handleError.bind(this))
      );
  }

  // Login user
  login(data: LoginRequest): Observable<LoginResponse> {
    console.log('Sending login request:', data);
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, data, { headers: this.getHeaders() })
      .pipe(
        tap(response => {
          if (!response?.token || !response?.role) {
            throw new Error('Invalid response from server');
          }
          this.setToken(response.token);
          this.setUserRole(response.role);
          this.isAuthenticatedSubject.next(true);
          this.redirectToDashboard(response.role);
        }),
        catchError(this.handleError.bind(this))
      );
  }

  // Get dashboard data
  getDashboard(): Observable<DashboardResponse> {
    const role = localStorage.getItem(this.userRoleKey);
    if (!role) {
      return throwError(() => new Error('No role found'));
    }
    
    const endpoint = role.toLowerCase();
    return this.http
      .get<DashboardResponse>(`${this.apiUrl}/roles/${endpoint}`, { headers: this.getAuthHeaders() })
      .pipe(
        tap(response => {
          this.userData.next({
            email: response.username,
            username: response.username
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
  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private setUserRole(role: string): void {
    localStorage.setItem(this.userRoleKey, role);
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  private redirectToDashboard(role: string): void {
    const roleLower = role.toLowerCase();
    switch (roleLower) {
      case 'viewer':
        this.router.navigate(['/dashboard/viewer']);
        break;
      case 'task_manager':
        this.router.navigate(['/dashboard/task-manager']);
        break;
      case 'participant':
        this.router.navigate(['/dashboard/participant']);
        break;
      default:
        this.router.navigate(['/dashboard']);
        break;
    }
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }

  private handleError(error: HttpErrorResponse) {
    console.error('API Error:', error);
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      if (error.status === 400) {
        errorMessage = error.error?.message || 'Invalid request data';
      } else if (error.status === 401) {
        errorMessage = 'Invalid credentials';
      } else if (error.status === 403) {
        errorMessage = 'Access denied';
      } else {
        errorMessage = error.error?.message || `Error: ${error.status} - ${error.statusText}`;
      }
    }
    
    return throwError(() => new Error(errorMessage));
  }

  private hasValidToken(): boolean {
    const token = this.getToken();
    return !!token;
  }
}
