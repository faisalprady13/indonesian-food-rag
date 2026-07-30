export interface CurrentUser {
  id: number;
  username: string;
  fullname: string;
  email: string;
  imageUrl: string | null;
  provider: string | null;
}

export interface RegisterPayload {
  username: string;
  email: string;
  password: string;
  fullname: string;
}
