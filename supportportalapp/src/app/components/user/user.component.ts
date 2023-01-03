import { HttpErrorResponse, HttpEvent, HttpEventType } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Subscription } from 'rxjs';
import { CustomHttpResponse } from 'src/app/model/CustomHttpResponse';
import { FileUploadStatus } from 'src/app/model/FileUploadStatus';
import { User } from 'src/app/model/User';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { UserService } from 'src/app/service/user.service';

@Component({
    selector: 'app-user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {
    private titleSubject = new BehaviorSubject<string>("Users");
    private subcriptions: Subscription[] = [];
    private currentUsername: string = "";

    public refreshing: boolean = false;
    public selectedUser: User | undefined;
    public titleAction$ = this.titleSubject.asObservable();
    public users: User[] = [];
    public fileName: String | null = null;
    public profileImage: File | null = null;
    public editUser = new User();
    public user: User | null = null;
    public fileStatus = new FileUploadStatus();

    constructor(
        private userService: UserService,
        private authenticationService: AuthenticationService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.getUsers();
        this.user = this.authenticationService.getUserFromLocalCache();
    }

    public changeTitle(title: string): void {
        this.titleSubject.next(title);
    }

    public getUsers(): void {
        this.refreshing = true;

        this.subcriptions.push(this.userService.getUsers().subscribe({
            next: (response: User[]) => {
                this.userService.addUsersToLocalCache(response);
                this.users = response;
                this.refreshing = false;
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse.error.message);
                this.refreshing = false;
            }
        }));
    }

    public onSelectUser(selectedUser: User): void {
        this.selectedUser = selectedUser;
        this.clickButton("openUserInfo");
    }

    public onProfileImageChange($event: Event): void {
        if ($event.target === null) return;
        const target: HTMLInputElement = $event.target as HTMLInputElement;
        if (target.files === null) return;

        const file = target.files[0];
        this.fileName = file.name;
        this.profileImage = file;
    }

    public saveNewUser(): void {
        this.clickButton("new-user-save");
    }

    public onAddNewUser(userForm: NgForm): void {
        const formData = this.userService.createUserFormData(null, userForm.value, this.profileImage);
        this.subcriptions.push(this.userService.addUser(formData).subscribe({
            next: (response: User) => {
                document.getElementById("new-user-close")?.click();
                this.getUsers();
                this.fileName = null;
                this.profileImage = null;
                userForm.reset();
                console.log(`${response.firstName} ${response.lastName} updated successfully!`);
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse);
            }
        }));
    }

    public searchUsers(searchTerm: string): void {
        const results: User[] = [];

        for (const user of this.userService.getUsersFromLocalCache()) {
            if (
                user.firstName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
                user.lastName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
                user.username.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
                user.userId?.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1
            ) {
                results.push(user);
            }
        }

        this.users = results;

        if (results.length === 0 || !searchTerm) {
            this.users = this.userService.getUsersFromLocalCache();
        }
    }

    public onEditUser(editUser: User): void {
        this.editUser = editUser;
        this.currentUsername = editUser.username;

        this.clickButton("openUserEdit");
    }

    public onUpdateUser(): void {
        const formData = this.userService.createUserFormData(this.currentUsername, this.editUser, this.profileImage);
        this.subcriptions.push(this.userService.addUser(formData).subscribe({
            next: (response: User) => {
                this.clickButton("closeEditUserModalButton")
                this.getUsers();
                this.fileName = null;
                this.profileImage = null;
                console.log(`${response.firstName} ${response.lastName} updated successfully!`);
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse);
            }
        }));
    }

    public onDeleteUser(userId: number): void {
        this.subcriptions.push(this.userService.deleteUser(userId).subscribe({
            next: (response: CustomHttpResponse) => {
                console.log(response.message);
                this.getUsers();
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse);
            }
        }));
    }

    public onResetPassword(emailForm: NgForm): void {
        this.refreshing = true;

        const emailAddress = emailForm.value["reset-password-email"];

        this.subcriptions.push(this.userService.resetPassword(emailAddress).subscribe({
            next: (response: CustomHttpResponse) => {
                console.log(response.message);
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse);
            },
            complete: () => {
                emailForm.reset();
                this.refreshing = false;
            }
        }))
    }

    public onUpdateCurrentUser(user: User): void {
        this.refreshing = true;
        this.currentUsername = this.authenticationService.getUserFromLocalCache().username;

        const formData = this.userService.createUserFormData(this.currentUsername, user, this.profileImage);
        this.subcriptions.push(this.userService.addUser(formData).subscribe({
            next: (response: User) => {
                this.authenticationService.addUserToLocalCache(response);
                this.getUsers();
                this.fileName = null;
                this.profileImage = null;
                console.log(`${response.firstName} ${response.lastName} updated successfully!`);
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse);
                this.profileImage = null;
            },
            complete: () => this.refreshing = false
        }));
    }

    public onLogout(): void {
        this.authenticationService.logOut();
        this.router.navigateByUrl("/login");
    }

    public updateProfileImage(): void {
        this.clickButton("profile-image-input");
    }

    public onUpdateProfileImage(): void {
        const formData = new FormData();
        if (this.user !== null) formData.append("username", this.user.username);
        if (this.profileImage !== null) formData.append("profileImage", this.profileImage);

        this.subcriptions.push(this.userService.updateProfileImage(formData).subscribe({
            next: (event: HttpEvent<any>) => {
                this.reportUploadProgress(event);
                this.fileStatus.status = "done";
                console.log("Profile image updated succssfully");
            },
            error: (httpErrorResponse: HttpErrorResponse) => {
                console.log(httpErrorResponse.error.message);
            }
        }))
    }

    private reportUploadProgress(event: HttpEvent<any>): void {
        switch (event.type) {
            case HttpEventType.UploadProgress:
                if (event.total) this.fileStatus.percentage = Math.round(100 * event.loaded / event.total);
                this.fileStatus.status = "progress";
                break;
            case HttpEventType.Response:
                if (event.status === 200 && this.user) {
                    this.user.profileImageUrl = `${event.body.profileImageUrl}?time=${new Date().getTime()}`;
                    console.log(`${event.body.firstName}'s profile image updated successfully`);
                    this.fileStatus.status = "done";
                } else {
                    console.log(`Unable to upload image. Please try again.`);
                }
                break;
        }
    }

    private clickButton(buttonId: string): void {
        document.getElementById(buttonId)?.click;
    }
}
