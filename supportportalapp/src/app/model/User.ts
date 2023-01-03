export class User {
    public id: number | null = null;
    public userId: string | null = null;
    public firstName: string;
    public lastName: string;
    public username: string;
    public email: string;
    public loginDateDisplay: Date | null = null;
    public joinDate: Date | null = null;
    public profileImageUrl: string | null = null;
    public active: boolean;
    public notLocked: boolean;
    public role: string;
    public authorities: string[];

    constructor() {
        this.firstName = "";
        this.lastName = "";
        this.username = "";
        this.email = "";
        this.active = false;
        this.notLocked = false;
        this.notLocked = false;
        this.role = "";
        this.authorities = [];
    }
}