import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { HeaderType } from 'src/app/enum/HeaderType.enum';
import { User } from 'src/app/model/User';
import { AuthenticationService } from 'src/app/service/authentication.service';

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {
    public showLoading: boolean = false;
    private subscriptions: Subscription[] = [];

    constructor(
        private router: Router,
        private authenticationService: AuthenticationService
    ) { }

    public onRegister(user: User): void {
        this.showLoading = true;
        this.subscriptions.push(this.authenticationService.register(user).subscribe({
            next: (response: HttpResponse<User>) => {
                this.showLoading = false;
                console.log(`An account has been created for ${response.body?.firstName}`);
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse);
                this.showLoading = false;
            }
        }));
    }

    ngOnInit(): void {
        if (this.authenticationService.isLoggedIn()) {
            this.router.navigateByUrl("/user/management");
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(subscription => subscription.unsubscribe());
    }
}
