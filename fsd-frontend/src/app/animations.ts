import { trigger, transition, style, animate, state } from '@angular/animations';

export const fadeIn = trigger('fadeIn', [
  transition(':enter', [
    style({ opacity: 0 }),
    animate('0.5s ease-in', style({ opacity: 1 }))
  ])
]);

export const slideUp = trigger('slideUp', [
  transition(':enter', [
    style({ transform: 'translateY(20px)', opacity: 0 }),
    animate('0.5s ease-out', style({ transform: 'translateY(0)', opacity: 1 }))
  ])
]);

export const zoomIn = trigger('zoomIn', [
  transition(':enter', [
    style({ transform: 'scale(0.95)', opacity: 0 }),
    animate('0.3s ease-out', style({ transform: 'scale(1)', opacity: 1 }))
  ])
]);

export const headerAnimation = trigger('headerAnimation', [
  state('normal', style({
    height: '80px',
    'background-color': 'rgba(255, 255, 255, 0.9)',
    'box-shadow': 'none'
  })),
  state('scrolled', style({
    height: '60px',
    'background-color': 'rgba(255, 255, 255, 0.95)',
    'box-shadow': '0 2px 10px rgba(0, 0, 0, 0.1)'
  })),
  transition('normal <=> scrolled', animate('0.3s ease-in-out'))
]); 