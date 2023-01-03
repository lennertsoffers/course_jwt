import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { User } from '../model/User';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private host = environment.apiUrl;
  private token: string | null = null;
  private loggedInUsername: string | null = null;
  private jwtHelperService = new JwtHelperService();

  constructor(private http: HttpClient) {}

  public login(user: User): Observable<HttpResponse<any> | HttpErrorResponse> {
    return this.http.post<HttpResponse<any> | HttpErrorResponse>(`${this.host}/user/login`, user, {observe: 'response'});
  }

  public register(user: User): Observable<HttpResponse<User> | HttpErrorResponse> {
    return this.http.post<HttpResponse<User> | HttpErrorResponse>(`${this.host}/user/register`, user, {observe: 'response'});
  }

  public logOut(): void {
    this.token = null;
    this.loggedInUsername = null;

    localStorage.removeItem("user");
    localStorage.removeItem("token");
    localStorage.removeItem("users");
  }

  public saveToken(token: string): void {
    this.token = token;  
    localStorage.setItem("token", token);
  }

  public addUserToLocalCache(user: User): void {
    localStorage.setItem("user", JSON.stringify(user));
  }

  public getUserFromLocalCache(): User {
    let user = localStorage.getItem("user")
    if (user === null) return new User();
    return JSON.parse(user);
  }

  public loadToken(): void {
    this.token = localStorage.getItem("token");
  }

  public getToken(): string {
    if (this.token === null) return "";
    return this.token;
  }

  public isLoggedIn(): boolean {
    this.loadToken();

    if (this.token !== null && this.token !== "") {
      if (this.jwtHelperService.decodeToken(this.token).sub !== null || '') {
        if (!this.jwtHelperService.isTokenExpired(this.token)) {
          this.loggedInUsername = this.jwtHelperService.decodeToken(this.token).sub;
  
          return true;
        }
      }
    }

    this.logOut();
    return false;
  }
}
