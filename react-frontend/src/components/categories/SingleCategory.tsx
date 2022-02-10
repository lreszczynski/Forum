import './SingleCategory.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Col, Modal, Row } from 'antd';
import Text from 'antd/lib/typography/Text';
import { AxiosError } from 'axios';
import React, { useState } from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { Link, useNavigate } from 'react-router-dom';
import { Category } from 'models/Category';
import AuthService from 'services/AuthService';
import CategoryService from 'services/CategoryService';
import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface ISingleCategoryProps {
  category: Category;
}

export default function SingleCategory({ category }: ISingleCategoryProps) {
  const categoryId = category.id;
  const navigate = useNavigate();
  const link = () => navigate(`/forum/${categoryId}`);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const canUserEditCategory = AuthService.canUserEditCategory(category);
  const canUserDeleteCategory = AuthService.canUserDeleteCategory(category);

  const queryClient = useQueryClient();

  const mutation = useMutation<void, AxiosError, number, any>((id: number) =>
    CategoryService.deleteCategory(id),
  );

  const categoryDiv = (
    <div role="link" tabIndex={0} onClick={link} onKeyPress={link}>
      <h2>{category.name}</h2>
      <span>{category.description}</span>
    </div>
  );

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleOk = () => {
    mutation.mutate(categoryId, {
      onSuccess: (_data, _variables) => {
        queryClient.invalidateQueries();
        notificationSuccessfulEdit();
      },
      onError: (error, _variables) => {
        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
    setIsModalVisible(false);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  if (canUserEditCategory || canUserDeleteCategory) {
    return (
      <div className="category">
        <Row>
          <Col flex="auto">{categoryDiv}</Col>
          <Col md={4} style={{ cursor: 'auto' }}>
            <Row justify="end" gutter={8}>
              {canUserEditCategory && (
                <Col>
                  <Link to={`/categories/${categoryId}`}>
                    <FontAwesomeIcon icon="edit" />
                  </Link>
                </Col>
              )}
              {canUserDeleteCategory && (
                <Col>
                  <div
                    style={{ cursor: 'pointer' }}
                    role="none"
                    onClick={() => showModal()}
                    onKeyDown={() => showModal()}
                  >
                    <Text type="danger">
                      <FontAwesomeIcon icon="trash" />
                    </Text>
                  </div>
                </Col>
              )}
            </Row>
          </Col>
        </Row>
        <Modal
          title="Confirm delete"
          visible={isModalVisible}
          onOk={handleOk}
          onCancel={handleCancel}
        >
          <Text>Do you want to delete &apos;{category.name}&apos;?</Text>
        </Modal>
      </div>
    );
  }
  return <div className="category">{categoryDiv}</div>;
}
