import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService, SignupRequest, SignupResponse } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isSubmitting = false;
  errorMessage = '';
  roles: string[] = ['USER', 'TASK_MANAGER'];
  roleDisplayNames: { [key: string]: string } = {
    'USER': 'User',
    'TASK_MANAGER': 'Event Manager'
  };

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  initForm(): void {
    this.registerForm = this.fb.group(
      {
        name: ['', [Validators.required, Validators.minLength(3)]],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
        clg_name: ['', [Validators.required, Validators.minLength(3)]],
        phone_no: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
        roles: ['', [Validators.required]]
      },
      {
        validators: this.passwordMatchValidator,
      }
    );
  }

  passwordMatchValidator(form: FormGroup): null | { mismatch: boolean } {
    const passwordControl = form.get('password');
    const confirmPasswordControl = form.get('confirmPassword');

    if (!passwordControl || !confirmPasswordControl) {
      return null;
    }

    const password = passwordControl.value;
    const confirmPassword = confirmPasswordControl.value;

    return password === confirmPassword ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const { name, email, password, clg_name, phone_no, roles } = this.registerForm.value;
    const signupData: SignupRequest = {
      name,
      email,
      password,
      clg_name,
      phone_no,
      roles
    };

    console.log('Sending signup data:', signupData);

    this.authService.signup(signupData).subscribe({
      next: (response: SignupResponse) => {
        console.log('Registration successful', response);
        this.isSubmitting = false;
        this.router.navigate(['/login']);
      },
      error: (error: Error) => {
        console.error('Registration error:', error);
        this.isSubmitting = false;
        this.errorMessage = error.message || 'Registration failed. Please try again later.';
      },
    });
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Helper methods for template
  get name() {
    return this.registerForm.get('name');
  }
  get email() {
    return this.registerForm.get('email');
  }
  get password() {
    return this.registerForm.get('password');
  }
  get confirmPassword() {
    return this.registerForm.get('confirmPassword');
  }
  get clg_name() {
    return this.registerForm.get('clg_name');
  }
  get phone_no() {
    return this.registerForm.get('phone_no');
  }
  get role() {
    return this.registerForm.get('roles');
  }
}
