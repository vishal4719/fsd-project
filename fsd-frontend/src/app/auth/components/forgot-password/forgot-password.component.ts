import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  isSubmitting = false;
  message = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotPasswordForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.message = '';

    const email = this.forgotPasswordForm.get('email')?.value;

    this.http.post('http://localhost:8080/api/auth/forgot-password', null, {
      params: { email },
      responseType: 'json'
    }).subscribe({
      next: (response: any) => {
        this.message = response.message;
        this.isSubmitting = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.error || 'An error occurred. Please try again.';
        this.isSubmitting = false;
      }
    });
  }

  get email() {
    return this.forgotPasswordForm.get('email');
  }
} 