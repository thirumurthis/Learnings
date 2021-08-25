##### Angular 11+

```
$ ng new simple-app --routing=false --style=css

$ ng generate component simple-component
```

- Remove content of the content from `app.component.html` and add.
```
<app-simple-component></app-simple-component>
```

- Update `simple-component.component.ts`
```ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl,FormGroup,FormArray } from '@angular/forms';

@Component({
  selector: 'app-simple-component',
  templateUrl: './simple-component.component.html',
  styleUrls: ['./simple-component.component.css']
})
export class SimpleComponentComponent implements OnInit {
   form : FormGroup;
   
   constructor(private fb: FormBuilder) { 
    this.form = this.fb.group({
      countries : ['']
    });
     this.countries = this.getCountries();
   }

  ngOnInit(): void {}

  countries = [
    { id: 1, name: "United States" },
    { id: 2, name: "Australia" },
    { id: 3, name: "Canada" },
    { id: 4, name: "Brazil" },
    { id: 5, name: "England" }
  ];

  getCountries (){
    return this.countries;
  }
  submit(){
    console.log("Form submitted: "+this.form?.value);
  }
 }
```
- update `simple-component.component.html` with below content
```html
<form [formGroup]="form" (ngSubmit)="submit()">
  <p>
     <select class="select-countries" formControlName="countries">
        <option [ngValue]="null" disabled>Select Country</option>
        <option *ngFor="let country of countries" [ngValue]="country.id">{{country.name}}</option>
      </select>
      <button type="submit">Submit</button>
    </p>
 </form>
```
- update `simple-component.component.spec.ts` content for test case
```ts
import { ComponentFixture, TestBed,waitForAsync } from '@angular/core/testing';
import { FormControl,FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';

import { SimpleComponentComponent } from './simple-component.component';

describe('SimpleComponentComponent', () => {
  let component: SimpleComponentComponent;
  let fixture: ComponentFixture<SimpleComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [],
      declarations: [ SimpleComponentComponent ],
      providers: [FormBuilder]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SimpleComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('check select option 3rd option canada', waitForAsync(() => {
    let select: HTMLSelectElement = fixture.debugElement.query(By.css('.select-countries')).nativeElement;
    select.value = select.options[3].value;
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      let text = select.options[select.selectedIndex].label;
      console.log(text);
      expect(text).toBe('Canada');
    });
  }));

    it('check option box 2nd for australia', waitForAsync(() => {
      let select: HTMLSelectElement = fixture.debugElement.query(By.css('.select-countries')).nativeElement;
      select.value = select.options[2].value;
      select.dispatchEvent(new Event('change'));
      fixture.detectChanges();
      fixture.whenStable().then(() => {
        let text = select.options[select.selectedIndex].label;
        console.log(text);
        expect(text).toBe('Australia');
      });  
  })); 
});
```
