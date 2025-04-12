import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, DashboardResponse } from '../../../auth/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  dashboardData: DashboardResponse | null = null;
  loading = true;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;

    this.authService.getDashboard().subscribe({
      next: (response) => {
        console.log('Dashboard data:', response);
        this.dashboardData = response;
        this.loading = false;
      },
      error: (error) => {
        console.error('Dashboard error:', error);
        this.error = error.message || 'Failed to load dashboard';
        this.loading = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}