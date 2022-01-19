import { Col, Row } from 'antd';
import Text from 'antd/lib/typography/Text';
import React, { useEffect, useState } from 'react';
import { Post } from 'models/Post';
import { breakpoints } from 'utils/screenWidth';

export interface ISinglePostProps {
  post: Post;
}

export default function SinglePost(props: ISinglePostProps) {
  const { post } = props;
  const [width, setWidth] = useState(window.innerWidth);
  useEffect(() => {
    function handleResize() {
      setWidth(window.innerWidth);
    }

    window.addEventListener('resize', handleResize);
  });
  let colour = '';
  switch (post.user.role.name) {
    case 'Moderator': {
      colour = 'green';
      break;
    }
    case 'Admin': {
      colour = 'red';
      break;
    }
    default: {
      break;
    }
  }
  let nick;
  if (colour !== '') {
    nick = (
      <Text
        style={{
          textDecoration: post.user.banned ? 'line-through' : 'e',
          color: colour,
        }}
      >
        {post.user.username}
      </Text>
    );
  } else {
    nick = (
      <Text
        style={{
          textDecoration: post.user.banned ? 'line-through' : 'e',
        }}
      >
        {post.user.username}
      </Text>
    );
  }

  if (width > breakpoints.xs) {
    return (
      <div className="postContainer">
        <Row>
          <Col span={24}>
            <div className="postHeader">
              <div className="avatar">
                {post.user.username.at(0)?.toUpperCase()}
              </div>
              {post.user.username}
            </div>
          </Col>
          <Col span={24}>
            <div className="postContent">{post.content}</div>
          </Col>
        </Row>
      </div>
    );
  }
  return (
    <div className="postContainer">
      <div className="postHeader">
        <Row align="middle" gutter={8}>
          <Col>
            <div className="avatar">
              <Text>{post.user.username.at(0)?.toUpperCase()}</Text>
            </div>
          </Col>
          <Col>
            {nick}
            <br />
            <Text type="secondary">{post.user.role.name}</Text>
          </Col>
        </Row>
      </div>
      <div className="postContent">{post.content}</div>
    </div>
  );
}
