import { Navigate, Outlet } from 'react-router-dom';
import AppSkeleton from '@/components/skeletons/AppSkeleton.tsx';
import type { CurrentUser } from '@/lib/api.ts';

type ProtectedRouteProps = {
  user: CurrentUser | null | undefined;
};

export default function ProtectedRoute(props: Readonly<ProtectedRouteProps>) {
  if (props.user === undefined) {
    return <AppSkeleton />;
  }

  return <>{props.user ? <Outlet /> : <Navigate to="/login" />}</>;
}
