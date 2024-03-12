<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge" />
    <title>Planbow</title>

    <style>


        .link-button:hover, .link-button:focus, .link-button:active {
            color: #fff;
            background-color: #499fb6;
            border-color: #499fb6;
        }
    </style>
    <link
            href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600&display=swap"
            rel="stylesheet"
    />
</head>
<body
        style="
      margin: 0;
      font-family: 'Poppins', sans-serif;
      background: #ffffff;
      font-size: 14px;
    "
>
<div
        style="
        max-width: 680px;
        margin: 0 auto;
        padding: 45px 30px 60px;
        background: #f4f7ff;
        background-image: url(https://archisketch-resources.s3.ap-northeast-2.amazonaws.com/vrstyler/1661497957196_595865/email-template-background-banner);
        background-repeat: no-repeat;
        background-size: 800px 452px;
        background-position: top center;
        font-size: 14px;
        color: #434343;
      "
>
    <header>
        <table style="width: 100%;">
            <tbody>
            <tr style="height: 0;">
                <td>
                    <img
                            style="display: block"
                            src="https://dev.idp.planbow.com/img/logo_white.png"
                            height="40"
                            width="150"
                    />
                </td>
                <td style="text-align: right;">
                <span
                        style="font-size: 16px; line-height: 30px; color: #ffffff;"
                >${date}</span
                >
                </td>
            </tr>
            </tbody>
        </table>
    </header>

    <main>
        <div
                style="
            margin: 0;
            margin-top: 70px;
            padding: 92px 30px 60px;
            background: #ffffff;
            border-radius: 30px;
            text-align: center;
          "
        >
            <div style="width: 100%; max-width: 489px; margin: 0 auto;">
                <h1
                        style="
                margin: 0;
                font-size: 24px;
                font-weight: 500;
                color: #1f1f1f;
              "
                >
                    Invite for ${planboardName}
                </h1>
                <p
                        style="
                margin: 0;
                margin-top: 17px;
                font-size: 16px;
                font-weight: 500;
              "
                >
                    Hey ${userName},
                </p>
                <p
                        style="
                margin: 0;
                margin-top: 17px;
                font-weight: 500;
                letter-spacing: 0.56px;
              "
                >
                    <b>${ownerName}</b> has invited you as <b>${role}</b> to planboard
                    <br/><br/>
                    <span style=" margin: 0;font-size: 30px;font-weight: 600;
                     color: #ba3d4f;">
                        "${planboardName}"
                   </span>
                <p style="margin-top: 60px;">
                    ${scheduleContent}

                </p>

                <p  style="
                margin: 0;
                margin-top: 60px;
                font-size: 30px;
                font-weight: 600;
                letter-spacing: 2px;
                color: #ba3d4f;
              ">
                    <a  style="border: 1px solid #ccc;
                        padding: 6px 12px;
                        text-align: center;
                        white-space: nowrap;
                        vertical-align: middle;
                        cursor: pointer;
                        background-image: none;
                        border: 1px solid transparent;
                        border-radius: 4px;
                        text-decoration: none;

                        color: #fff;
                        background-color: #0e69e8;
                        border-color: #0e69e8;" href="${verifyUrl}">
                        Accept & Explore
                    </a>
                </p>
            </div>
        </div>

        <p
                style="
            max-width: 400px;
            margin: 0 auto;
            margin-top: 90px;
            text-align: center;
            font-weight: 500;
            color: #8c8c8c;
          "
        >
            Need help? Ask at
            <a
                    href="mailto:planbowdocs@gmail.com"
                    style="color: #499fb6; text-decoration: none;"
            >planbowdocs@gmail.com</a
            >
            or visit our
            <a
                    href=""
                    target="_blank"
                    style="color: #499fb6; text-decoration: none;"
            >Help Center</a
            >
        </p>
    </main>

    <footer
            style="
          width: 100%;
          max-width: 490px;
          margin: 20px auto 0;
          text-align: center;
          border-top: 1px solid #e6ebf1;
        "
    >
        <p
                style="
            margin: 0;
            margin-top: 40px;
            font-size: 16px;
            font-weight: 600;
            color: #434343;
          "
        >
            Planbow Company
        </p>
        <p style="margin: 0; margin-top: 8px; color: #434343;">
            Pebble Bay, Bhopal, Madhya Pradesh , India.
        </p>
        <div style="margin: 0; margin-top: 16px;">
            <a href="" target="_blank" style="display: inline-block;">
                <img
                        width="36px"
                        alt="Facebook"
                        src="https://archisketch-resources.s3.ap-northeast-2.amazonaws.com/vrstyler/1661502815169_682499/email-template-icon-facebook"
                />
            </a>
            <a
                    href=""
                    target="_blank"
                    style="display: inline-block; margin-left: 8px;"
            >
                <img
                        width="36px"
                        alt="Instagram"
                        src="https://archisketch-resources.s3.ap-northeast-2.amazonaws.com/vrstyler/1661504218208_684135/email-template-icon-instagram"
                /></a>
            <a
                    href=""
                    target="_blank"
                    style="display: inline-block; margin-left: 8px;"
            >
                <img
                        width="36px"
                        alt="Twitter"
                        src="https://archisketch-resources.s3.ap-northeast-2.amazonaws.com/vrstyler/1661503043040_372004/email-template-icon-twitter"
                />
            </a>
            <a
                    href=""
                    target="_blank"
                    style="display: inline-block; margin-left: 8px;"
            >
                <img
                        width="36px"
                        alt="Youtube"
                        src="https://archisketch-resources.s3.ap-northeast-2.amazonaws.com/vrstyler/1661503195931_210869/email-template-icon-youtube"
                /></a>
        </div>
        <p style="margin: 0; margin-top: 16px; color: #434343;">
            Copyright © 2024 Company. All rights reserved.
        </p>
    </footer>
</div>
</body>
</html>
