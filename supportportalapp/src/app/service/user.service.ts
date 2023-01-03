import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpEvent } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { User } from '../model/User';
import { CustomHttpResponse } from '../model/CustomHttpResponse';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private host = environment.apiUrl;

    constructor(private http: HttpClient) { }

    public getUsers(): Observable<User[]> {
        return this.http.get<User[]>(`${this.host}/user/list`);
    }

    public addUser(formData: FormData): Observable<User> {
        return this.http.post<User>(`${this.host}/user/add`, formData);
    }

    public updateUser(formData: FormData): Observable<User> {
        return this.http.post<User>(`${this.host}/user/update`, formData);
    }

    public resetPassword(email: string): Observable<CustomHttpResponse> {
        return this.http.get<CustomHttpResponse>(`${this.host}/user/resetpassword/${email}`);
    }

    public updateProfileImage(formData: FormData): Observable<HttpEvent<User>> {
        return this.http.post<User>(`${this.host}/user/updateProfileImage`, formData, { reportProgress: true, observe: "events" });
    }

    public deleteUser(userId: number): Observable<CustomHttpResponse> {
        return this.http.delete<CustomHttpResponse>(`${this.host}/user/delete/${userId}`);
    }

    public addUsersToLocalCache(users: User[]): void {
        localStorage.setItem("users", JSON.stringify(users));
    }

    public getUsersFromLocalCache(): User[] {
        const users = localStorage.getItem("users");

        if (users) return JSON.parse(users);
        return [];
    }

    public createUserFormData(loggedInUseranme: string | null, user: User, profileImage: File | null): FormData {
        const formData = new FormData();
        if (loggedInUseranme !== null) formData.append("currentUsername", loggedInUseranme);
        formData.append("firstName", user.firstName);
        formData.append("lastName", user.lastName);
        formData.append("username", user.username);
        formData.append("email", user.email);
        formData.append("role", user.role);
        if (profileImage !== null) formData.append("profileImage", profileImage);
        formData.append("isActive", JSON.stringify(user.active));
        formData.append("isNonLocked", JSON.stringify(user.notLocked));
        return formData;
    }
}
