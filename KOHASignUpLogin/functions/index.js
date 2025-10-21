const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

// Configure Gmail transporter
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "roledaalessandra@gmail.com", // your Gmail
    pass: "icuhqqcjdhxguhwe", // your 16-character app password, no spaces
  },
});

exports.sendOtpEmail = functions.https.onRequest(async (req, res) => {
  // Allow requests from your Android app
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(204).send("");
  }

  const {email, otp} = req.body;

  if (!email || !otp) {
    return res.status(400).send("Missing email or OTP");
  }

  const mailOptions = {
    from: "KOHA Team <roledaalessandra@gmail.com>",
    to: email,
    subject: "Your KOHA OTP Code",
    text: `Your OTP code is: ${otp}`,
  };

  try {
    await transporter.sendMail(mailOptions);
    res.status(200).send("Email sent successfully!");
  } catch (error) {
    console.error(error);
    res.status(500).send("Failed to send email: " + error.toString());
  }
});
