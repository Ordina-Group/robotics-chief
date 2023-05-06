export enum Status {
  Loading = 'Loading',
  StaleWhileRevalidate = 'StaleWhileRevalidate',
  Done = 'Done',
}

export enum ResultType {
  Empty = 'Empty',
  Success = 'Success',
  Failure = 'Failure',
}

export type State<T, S = undefined> = LoadingState<T, S> | SuccessState<T, S> | FailureState<T, S>;

export interface LoadingState<T, S> {
  status: Status.Loading;
  type: ResultType.Empty;
  result: undefined;
}

export interface SuccessState<T, S> {
  status: Status.Done | Status.StaleWhileRevalidate;
  type: ResultType.Success;
  result: T;
}

export interface FailureState<T, S> {
  status: Status.Done | Status.StaleWhileRevalidate;
  type: ResultType.Failure;
  result: S;
}
