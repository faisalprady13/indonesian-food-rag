import { Navigate, Outlet } from 'react-router-dom';
import type { CurrentUser } from '@/lib/api.ts';

type ProtectedRouteProps = {
  user: CurrentUser | null | undefined;
};

export default function ProtectedRoute(props: Readonly<ProtectedRouteProps>) {
  if (props.user === undefined) {
    return <h1>loading...</h1>;
  }

  return <>{props.user ? <Outlet /> : <Navigate to="/login" />}</>;
}
