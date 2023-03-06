<?php

namespace RemoteCall\NinjaForms;

use RemoteCall\Db\MaestroDatabaseQueries;
use RemoteCall\Exception\Validation;
use RemoteCall\Form\DefinitionMapper;
use RemoteCall\Form\Definitions;
use RemoteCall\Form\S3\S3ImageUpload;

class SubscriberSubmission
{

    public function submit($subscriberId, $subscriberData)
    {
        $subscriberDefinitionsToValues = DefinitionMapper::from_array($subscriberData, Definitions::get_subscriber_data_definition());

        error_log("DEBUG Subscriber def/values " . json_encode($subscriberDefinitionsToValues));

        $fieldToValue = DefinitionMapper::fromDefinitions($subscriberDefinitionsToValues['definitions'], $subscriberDefinitionsToValues['values'], $subscriberId);

        error_log("DEBUG Subscriber fieldtovalue " . json_encode($fieldToValue));

        // Update subscriber
        $maestroDatabaseQueries = new MaestroDatabaseQueries();

        $queryResult = $maestroDatabaseQueries->checkSubscriberUniqueness($subscriberId, $fieldToValue);
        if ($queryResult !== null) {
            Validation::fail("Subscriber update failed, the website url is not unique");
        }


        $queryResult = $maestroDatabaseQueries->updateSubscriber($subscriberId, $fieldToValue);
        Validation::nonNull($queryResult, "Subscriber update failed for specialist %s", $_SESSION["specialistid"]);

        // update S3
        $s3ImageUpload = new S3ImageUpload();
        if ($fieldToValue['logo']->isValidUpload()) {
            $s3ImageUpload->uploadImageData($fieldToValue['logo']);
        } else if ($fieldToValue['logo']->isValidDelete()) {
            $s3ImageUpload->deleteImage($fieldToValue['logo']);
        }

        if ($fieldToValue['icon']->isValidUpload()) {
            $s3ImageUpload->uploadImageData($fieldToValue['icon']);
        } else if ($fieldToValue['icon']->isValidDelete()) {
            $s3ImageUpload->deleteImage($fieldToValue['icon']);
        }
    }
}
