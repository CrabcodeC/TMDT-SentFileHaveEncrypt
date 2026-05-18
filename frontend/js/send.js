const form = document.getElementById("sendForm");

form.addEventListener("submit", async function (e) {
  e.preventDefault();

  const fileInput = document.getElementById("file");

  const formData = new FormData();

  formData.append("file", fileInput.files[0]);

  const status = document.getElementById("status");

  status.innerHTML = "Processing...";

  try {
    const response = await fetch("http://localhost:8080/send", {
      method: "POST",
      body: formData,
    });

    const data = await response.json();

    status.innerHTML = `
      <p>✓ File Sent</p>
      <p>Hash Generated</p>
      <p>Digital Signature Created</p>
    `;

    console.log(data);
  } catch (error) {
    status.innerHTML = "Error Sending File";
  }
});
