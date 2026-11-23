import { Component, input, output } from '@angular/core';

/** 1-5 Sterne: lesend (readonly) oder klickbar, dann wird die gewaehlte Sternzahl ueber "rated" gemeldet. */
@Component({
  selector: 'app-star-rating',
  templateUrl: './star-rating.html'
})
export class StarRating {
  readonly value = input<number>(0);
  readonly readonly = input(false);
  readonly label = input('Bewertung');
  readonly rated = output<number>();

  protected readonly stars = [1, 2, 3, 4, 5];
}
