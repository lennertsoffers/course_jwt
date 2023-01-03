import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { HeaderType } from 'src/app/enum/HeaderType.enum';
import { User } from 'src/app/model/User';
import { AuthenticationService } from 'src/app/service/authentication.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {
    public showLoading: boolean = false;
    private subscriptions: Subscription[] = [];

    constructor(
        private router: Router,
        private authenticationService: AuthenticationService
    ) { }

    public onLogin(user: User): void {
        this.showLoading = true;
        this.subscriptions.push(this.authenticationService.login(user).subscribe({
            next: (response: HttpResponse<User>) => {
                const token = response.headers.get(HeaderType.JWT_TOKEN);

                if (token !== null && response.body !== null) {
                    this.authenticationService.saveToken(token);
                    this.authenticationService.addUserToLocalCache(response.body);

                    this.router.navigateByUrl("/user/management");
                    this.showLoading = false;
                }
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
        } else {
            this.router.navigateByUrl("/login");
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(subscription => subscription.unsubscribe());
    }
}
