import { Col, Divider, Row } from 'antd';
import Text from 'antd/lib/typography/Text';
import moment from 'moment';
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
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);
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

  if (width > breakpoints.sm) {
    return (
      <div className="postContainer">
        <Row style={{ minHeight: 'inherit' }}>
          <Col span="100px">
            <div
              className="postHeader"
              style={{ minHeight: '100%', width: '120px' }}
            >
              <Row align="middle" gutter={8}>
                <Col span={24}>
                  <div className="avatar">
                    <Text>{post.user.username.at(0)?.toUpperCase()}</Text>
                  </div>
                </Col>
                <Col span={24} style={{ textAlign: 'center' }}>
                  {nick}
                  <br />
                  <Text type="secondary">{post.user.role.name}</Text>
                </Col>
              </Row>
            </div>
          </Col>
          <Col span={18}>
            <div className="postContent" style={{ minHeight: '100%' }}>
              <Text type="secondary">
                {moment(post.createDate).format('L')}
              </Text>
              <Divider style={{ margin: '4px 0px' }} />
              {post.content}
            </div>
          </Col>
        </Row>
      </div>
    );
  }
  return (
    <div className="postContainer">
      <div className="postHeader">
        <Row style={{ height: 'inherit' }} align="middle" gutter={8}>
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
      <div className="postContent" style={{ minHeight: '100%' }}>
        <Text type="secondary">{moment(post.createDate).format('L')}</Text>
        <Divider style={{ margin: '4px 0px' }} />
        {post.content}
      </div>
    </div>
  );
}
