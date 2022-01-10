import './singleThread.scss';

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Thread } from 'models/Thread';

export default function SingleThread(params: { id: number; thread: Thread }) {
  const { thread } = params;
  // const threadId = thread.id;
  const navigate = useNavigate();
  const link = () => navigate(`/forum/`, { replace: true });

  return (
    <div
      className="thread"
      role="link"
      tabIndex={0}
      onClick={link}
      onKeyPress={link}
    >
      <h2>{thread.title}</h2>
    </div>
  );
}
