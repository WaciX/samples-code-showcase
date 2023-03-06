<?php

namespace RemoteCall\Form;

use RemoteCall\Form\S3\S3FormDataDefinition;

class Definitions
{
    public static function get_subscriber_data_definition(): array
    {
        return array(
            'company_name' => new DbFormDataDefinition('Company Name',
                new FormDataValidation(FormDataType::String, false, 45),
                'companyNm'),
            'welcome_msg' => new DbFormDataDefinition('Welcome Message',
                new FormDataValidation(FormDataType::String, true, 128),
                'welcomeMsg'),
            'branding_accent_color' => new DbFormDataDefinition('Branding Accent Color',
                new FormDataValidation(FormDataType::StringColorHex, true, 6),
                'accentRGB'),
            'terms' => new DbFormDataDefinition('Terms and Conditions',
                new FormDataValidation(FormDataType::String, true, 65535),
                'terms'),
            'website' => new DbFormDataDefinition('Website Url',
                new FormDataValidation(FormDataType::Url, false, 45),
                'wwwUrl'),
            'phone_number' => new DbFormDataDefinition('Phone Number',
                new FormDataValidation(FormDataType::String, false, 20),
                'phoneNumber'),
            'timezone' => new DbFormDataDefinition('Time Zone',
                new FormDataValidation(FormDataType::Timezone, false, 64),
                'timeZone'),
            'region' => new DbFormDataDefinition('Region',
                new FormDataValidation(FormDataType::LocationRegion, true, 15),
                'proxy_group'),
            'beta' => new DbFormDataDefinition('Try Beta',
                new FormDataValidation(FormDataType::Beta, false, 10),
                'downloadBuild'),
            'logo' => new S3FormDataDefinition('Splash Screen & Support Portal Logo',
                new FormDataValidation(FormDataType::ImagePngBase64, true, 524_288),
                S3_BRANDING_PUBLIC_URL, S3_BRANDING_BUCKET, 'logos/', '.png'),
            'icon' => new S3FormDataDefinition('Customer Desktop icon',
                new FormDataValidation(FormDataType::ImageIcoBase64, true, 524_288),
                S3_BRANDING_PUBLIC_URL, S3_BRANDING_BUCKET, 'icons/', '.ico'),
            'icon_text' => new DbFormDataDefinition('Icon Text',
                new FormDataValidation(FormDataType::String, true, 64),
                'iconText')
        );
    }

    public static function get_specialist_data_definition(): array
    {
        return array(
            'id' => new DbFormDataDefinition('Id',
                new FormDataValidation(FormDataType::Email, false, 64),
                'id'),
            'name' => new DbFormDataDefinition('Name',
                new FormDataValidation(FormDataType::String, false, 45),
                'name'),
            'active' => new DbFormDataDefinition('Is Active',
                new FormDataValidation(FormDataType::String, false, 1),
                'active'),
            'requiresCustInfo' => new DbFormDataDefinition('Requires Customer Info',
                new FormDataValidation(FormDataType::Number, true, 1),
                'requiresCustInfo'),
            'photo' => new S3FormDataDefinition('Profile Photo',
                new FormDataValidation(FormDataType::ImagePngBase64, true, 524_288),
                S3_BRANDING_PUBLIC_URL, S3_BRANDING_BUCKET, 'headshots/', '.png'),
        );
    }
}
