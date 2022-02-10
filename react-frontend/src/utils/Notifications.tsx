import { notification } from 'antd';

export function notificationError() {
  notification.error({
    duration: 0,
    message: 'Connection problem',
    description: 'Could not connect to the server.',
    onClick: () => console.log('Notification Clicked!'),
  });
}

export function notificationErrorStatusCode(error: number) {
  switch (error) {
    case 400:
      notification.error({
        duration: 3,
        message: 'Bad Request',
        description: '400 Bad Request.',
      });
      break;

    case 401:
      notification.error({
        duration: 3,
        message: 'Unauthorized',
        description: '401 Unauthorized.',
      });
      break;

    case 403:
      notification.error({
        duration: 3,
        message: 'Forbidden',
        description: '403 Forbidden.',
      });
      break;

    default:
      notification.error({
        duration: 3,
        message: 'Connection problem',
        description: 'Could not connect to the server.',
      });
      break;
  }
}

export function notificationSuccessfulEdit() {
  notification.success({
    duration: 3,
    message: 'Saved',
    description: 'Changes have been saved.',
    onClick: () => console.log('Notification Clicked!'),
  });
}
