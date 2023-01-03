import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { BehaviorSubject, Subscription } from 'rxjs';
import { User } from 'src/app/model/User';
import { UserService } from 'src/app/service/user.service';

@Component({
    selector: 'app-user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {
    private titleSubject = new BehaviorSubject<string>("Users");
    private subcriptions: Subscription[] = [];

    public refreshing: boolean = false;
    public selectedUser: User | undefined;
    public titleAction$ = this.titleSubject.asObservable();
    public users: User[] = [];
    public fileName: String | null = null;
    public profileImage: File | null = null;

    constructor(private userService: UserService) { }

    ngOnInit(): void {
        this.getUsers();
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

    public onSelectUser(selectedUser: User) {
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

    private clickButton(buttonId: string): void {
        document.getElementById(buttonId)?.click;
    }
}
