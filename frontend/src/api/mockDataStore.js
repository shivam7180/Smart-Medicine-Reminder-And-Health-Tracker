// Intelligent In-Browser Data Store & Real-Life Healthcare Simulation Engine
// Dynamically scopes all medicines, doses, schedules, vitals, and goals by the active user's ID.

const STORAGE_KEY = 'medicare_app_v6_clean';

// Clean old legacy test keys from previous sessions
try {
  localStorage.removeItem('medicare_local_db');
  localStorage.removeItem('medicare_app_v2_db');
  localStorage.removeItem('medicare_app_v3_clean');
  localStorage.removeItem('medicare_app_v4_clean');
  localStorage.removeItem('medicare_app_v5_clean');
} catch (e) {}

// Clean initial state with ZERO pre-filled user medications, doses, or demo doctors
const initialDb = {
  users: [],
  medicines: [],
  schedules: [],
  doses: [],
  vitals: {},      // Map of userId -> latest vitals object
  vitalLogs: [],   // Array of { id, userId, recordType, valuePrimary, valueSecondary, readingTime, notes }
  goals: [],
  appointments: [],
  notifications: [],
  doctors: []      // ZERO fake/demo doctors. Only real doctors who register appear here!
};

export const getDb = () => {
  try {
    const data = localStorage.getItem(STORAGE_KEY);
    if (!data) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(initialDb));
      return initialDb;
    }
    const parsed = JSON.parse(data);
    // Ensure no legacy pre-seeded demo doctors persist
    if (parsed.doctors) {
      parsed.doctors = parsed.doctors.filter(d => d.isRegisteredUser || (d.userId && d.userId > 100));
    }
    return parsed;
  } catch (e) {
    return initialDb;
  }
};

export const saveDb = (db) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(db));
  } catch (e) {
    console.error('Failed to save to local DB', e);
  }
};

function getCurrentUserId() {
  try {
    const user = JSON.parse(localStorage.getItem('medicare_user') || '{}');
    return user.id || 0;
  } catch (e) {
    return 0;
  }
}

// Route and Mock Response Dispatcher
export function handleMockRoute(method, url, data) {
  const db = getDb();
  const cleanUrl = url.split('?')[0];
  const currentUserId = getCurrentUserId();

  // 1. Auth: Register
  if (cleanUrl.endsWith('/api/v1/auth/register')) {
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    const newId = Date.now();
    const role = (body.role || 'PATIENT').toUpperCase();
    const userRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;

    const newUser = {
      id: newId,
      email: body.email,
      fullName: body.fullName || 'New User',
      roles: [userRole],
      isActive: true,
      phoneNumber: body.phoneNumber || '',
      bloodGroup: body.bloodGroup || 'O+',
      gender: body.gender || 'MALE'
    };

    db.users.push(newUser);

    // If registering as DOCTOR, immediately create their active Physician Profile!
    if (role === 'DOCTOR') {
      const docFullName = body.fullName?.startsWith('Dr.') ? body.fullName : `Dr. ${body.fullName}`;
      const newDoctor = {
        id: newId,
        userId: newId,
        fullName: docFullName,
        email: body.email,
        specializationName: body.specializationName || body.specialization || 'Cardiology',
        qualification: body.qualification || 'MD, MBBS',
        experienceYears: parseInt(body.experienceYears, 10) || 8,
        consultationFee: parseFloat(body.consultationFee) || 100.00,
        bio: body.bio || `Board-certified medical physician in ${body.specializationName || 'Cardiology'}.`,
        availableDays: body.availableDays || 'MON,TUE,WED,THU,FRI',
        isRegisteredUser: true,
        createdAt: new Date().toISOString()
      };
      // Place newly registered real doctor at the top so patients see them immediately!
      db.doctors.unshift(newDoctor);

      db.notifications.push({
        id: Date.now(),
        userId: newId,
        title: 'Doctor Profile Activated!',
        message: `Welcome ${docFullName}. Your clinical profile in ${newDoctor.specializationName} is now live. Patients can view your credentials and book consultations directly.`,
        notificationType: 'SYSTEM',
        isRead: false,
        createdAt: new Date().toISOString()
      });
    } else {
      // Initial welcome notification for new patient
      db.notifications.push({
        id: Date.now(),
        userId: newId,
        title: 'Welcome to MediCare!',
        message: 'Your personal health account is ready. Begin by adding your prescribed medications to enable automated dose reminders.',
        notificationType: 'SYSTEM',
        isRead: false,
        createdAt: new Date().toISOString()
      });
    }

    saveDb(db);

    return {
      success: true,
      message: 'Account registered successfully',
      data: {
        accessToken: `jwt_token_${newId}`,
        refreshToken: `refresh_token_${newId}`,
        userId: newId,
        email: body.email,
        fullName: body.fullName,
        roles: [userRole],
        expiresIn: 86400000
      }
    };
  }

  // 2. Auth: Login
  if (cleanUrl.endsWith('/api/v1/auth/login')) {
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    let user = db.users.find((u) => u.email?.toLowerCase() === body.email?.toLowerCase());

    if (!user) {
      // Create user on the fly if non-existent
      user = {
        id: Date.now(),
        email: body.email,
        fullName: body.email?.split('@')[0] || 'User',
        roles: ['ROLE_PATIENT'],
        isActive: true
      };
      db.users.push(user);
      saveDb(db);
    }

    return {
      success: true,
      message: 'Login successful',
      data: {
        accessToken: `jwt_token_${user.id}`,
        refreshToken: `refresh_token_${user.id}`,
        userId: user.id,
        email: user.email,
        fullName: user.fullName,
        roles: user.roles,
        expiresIn: 86400000
      }
    };
  }

  // 3. User Profile
  if (cleanUrl.endsWith('/api/v1/users/me')) {
    const user = db.users.find(u => u.id === currentUserId) || {
      id: currentUserId,
      email: 'user@medicare.com',
      fullName: 'Patient',
      roles: ['ROLE_PATIENT'],
      isActive: true
    };
    return { success: true, data: user };
  }

  if (cleanUrl.endsWith('/api/v1/users/profile')) {
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    const idx = db.users.findIndex(u => u.id === currentUserId);
    if (idx !== -1) {
      db.users[idx] = { ...db.users[idx], ...body };
      saveDb(db);
    }
    return { success: true, message: 'Profile updated', data: body };
  }

  // 4. Patient Dashboard Aggregation (Scoped strictly to currentUserId)
  if (cleanUrl.endsWith('/api/v1/analytics/dashboard/patient')) {
    const userMeds = db.medicines.filter((m) => m.patientId === currentUserId && m.isActive);
    const userDoses = db.doses.filter((d) => d.patientId === currentUserId);

    const totalScheduled = userDoses.length;
    const taken = userDoses.filter((d) => d.status === 'TAKEN').length;
    const skipped = userDoses.filter((d) => d.status === 'SKIPPED').length;
    const missed = userDoses.filter((d) => d.status === 'MISSED').length;
    const pending = userDoses.filter((d) => d.status === 'SCHEDULED' || d.status === 'SNOOZED').length;
    const adherenceRate = totalScheduled > 0 ? Math.round((taken / totalScheduled) * 100) : 100;

    const nextUpcoming = userDoses.find((d) => d.status === 'SCHEDULED' || d.status === 'SNOOZED') || null;

    const userVitals = db.vitals[currentUserId] || {};
    const userBpTrends = db.vitalLogs.filter(v => v.userId === currentUserId && v.recordType === 'BLOOD_PRESSURE');
    const userSugarTrends = db.vitalLogs.filter(v => v.userId === currentUserId && v.recordType === 'BLOOD_SUGAR');
    const userAppts = db.appointments.filter(a => a.patientId === currentUserId);

    return {
      success: true,
      data: {
        activeMedicinesCount: userMeds.length,
        todayReminders: {
          totalScheduledToday: totalScheduled,
          takenToday: taken,
          skippedToday: skipped,
          missedToday: missed,
          pendingToday: pending,
          adherenceRateToday: adherenceRate,
          nextUpcomingReminder: nextUpcoming,
          todayDoses: userDoses
        },
        adherenceStats: {
          adherencePercentage: adherenceRate,
          totalScheduled,
          takenCount: taken
        },
        latestVitals: userVitals,
        bloodPressureTrends: userBpTrends,
        bloodSugarTrends: userSugarTrends,
        upcomingAppointments: userAppts
      }
    };
  }

  // 5. Today's Reminders Summary
  if (cleanUrl.endsWith('/api/v1/doses/today/summary')) {
    const userDoses = db.doses.filter((d) => d.patientId === currentUserId);
    const totalScheduled = userDoses.length;
    const taken = userDoses.filter((d) => d.status === 'TAKEN').length;
    const skipped = userDoses.filter((d) => d.status === 'SKIPPED').length;
    const missed = userDoses.filter((d) => d.status === 'MISSED').length;
    const pending = userDoses.filter((d) => d.status === 'SCHEDULED' || d.status === 'SNOOZED').length;
    const adherenceRate = totalScheduled > 0 ? Math.round((taken / totalScheduled) * 100) : 100;

    return {
      success: true,
      data: {
        totalScheduledToday: totalScheduled,
        takenToday: taken,
        skippedToday: skipped,
        missedToday: missed,
        pendingToday: pending,
        adherenceRateToday: adherenceRate,
        todayDoses: userDoses
      }
    };
  }

  // Dose Take
  if (cleanUrl.match(/\/api\/v1\/doses\/\d+\/take$/)) {
    const doseId = parseInt(cleanUrl.split('/')[4], 10);
    const target = db.doses.find((d) => d.id === doseId && d.patientId === currentUserId);
    if (target) {
      target.status = 'TAKEN';
      target.actualTime = new Date().toISOString();

      // Deduct 1 stock from medicine inventory if present
      const med = db.medicines.find((m) => m.id === target.medicineId);
      if (med && med.currentStock > 0) {
        med.currentStock -= 1;
        if (med.currentStock <= med.minimumThreshold) {
          med.isLowStock = true;
        }
      }
      saveDb(db);
    }
    return { success: true, message: 'Dose marked as TAKEN', data: target };
  }

  // Dose Skip
  if (cleanUrl.match(/\/api\/v1\/doses\/\d+\/skip$/)) {
    const doseId = parseInt(cleanUrl.split('/')[4], 10);
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    const target = db.doses.find((d) => d.id === doseId && d.patientId === currentUserId);
    if (target) {
      target.status = 'SKIPPED';
      target.skipReason = body.reason || 'Skipped by patient';
      saveDb(db);
    }
    return { success: true, message: 'Dose marked as SKIPPED', data: target };
  }

  // Dose Snooze
  if (cleanUrl.match(/\/api\/v1\/doses\/\d+\/snooze$/)) {
    const doseId = parseInt(cleanUrl.split('/')[4], 10);
    const target = db.doses.find((d) => d.id === doseId && d.patientId === currentUserId);
    if (target) {
      target.status = 'SNOOZED';
      target.snoozeCount = (target.snoozeCount || 0) + 1;
      saveDb(db);
    }
    return { success: true, message: 'Dose snoozed for 15 minutes', data: target };
  }

  // 6. Medicines Management (Scoped to currentUserId)
  if (cleanUrl.endsWith('/api/v1/medicines')) {
    if (method.toLowerCase() === 'post') {
      const body = typeof data === 'string' ? JSON.parse(data) : data || {};
      const newMedId = Date.now();
      const newMed = {
        id: newMedId,
        patientId: currentUserId,
        ...body,
        currentStock: body.initialStock || 30,
        minimumThreshold: body.minimumThreshold || 5,
        isLowStock: false,
        isActive: true,
        createdAt: new Date().toISOString()
      };
      db.medicines.push(newMed);
      saveDb(db);
      return { success: true, message: 'Medicine added successfully', data: newMed };
    }

    const userMeds = db.medicines.filter((m) => m.patientId === currentUserId && m.isActive);
    return {
      success: true,
      data: {
        content: userMeds
      }
    };
  }

  if (cleanUrl.match(/\/api\/v1\/medicines\/\d+$/) && method.toLowerCase() === 'delete') {
    const medId = parseInt(cleanUrl.split('/')[4], 10);
    const med = db.medicines.find((m) => m.id === medId && m.patientId === currentUserId);
    if (med) {
      med.isActive = false;
      saveDb(db);
    }
    return { success: true, message: 'Medicine removed' };
  }

  // 7. Schedules & Doses Generation
  if (cleanUrl.endsWith('/api/v1/schedules/active')) {
    return {
      success: true,
      data: db.schedules.filter((s) => s.patientId === currentUserId)
    };
  }

  if (cleanUrl.endsWith('/api/v1/schedules') && method.toLowerCase() === 'post') {
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    const schedId = Date.now();
    const newSched = {
      id: schedId,
      patientId: currentUserId,
      ...body
    };
    db.schedules.push(newSched);

    // Automatically create today's scheduled doses for each reminder time
    const todayStr = new Date().toISOString().substring(0, 10);
    (body.reminderTimes || []).forEach((timeStr, idx) => {
      const timeClean = timeStr.length === 5 ? `${timeStr}:00` : timeStr;
      db.doses.push({
        id: Date.now() + idx,
        scheduleId: schedId,
        patientId: currentUserId,
        medicineId: body.medicineId,
        medicineName: body.medicineName,
        scheduledTime: `${todayStr}T${timeClean}`,
        actualTime: null,
        status: 'SCHEDULED',
        snoozeCount: 0
      });
    });

    saveDb(db);
    return { success: true, message: 'Schedule and reminder times created', data: newSched };
  }

  if (cleanUrl.endsWith('/api/v1/doses/generate')) {
    return { success: true, message: 'Doses generated' };
  }

  if (cleanUrl.endsWith('/api/v1/doses/history')) {
    const userDoses = db.doses.filter(d => d.patientId === currentUserId);
    return {
      success: true,
      data: {
        content: userDoses
      }
    };
  }

  // 8. Health Vitals (Scoped to currentUserId)
  if (cleanUrl.endsWith('/api/v1/health/vitals/latest')) {
    return {
      success: true,
      data: db.vitals[currentUserId] || {}
    };
  }

  if (cleanUrl.endsWith('/api/v1/health/trends')) {
    const isSugar = url.includes('BLOOD_SUGAR');
    const logs = db.vitalLogs.filter(
      v => v.userId === currentUserId && v.recordType === (isSugar ? 'BLOOD_SUGAR' : 'BLOOD_PRESSURE')
    );
    return { success: true, data: logs };
  }

  if (cleanUrl.endsWith('/api/v1/health/records')) {
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    const type = body.recordType;

    if (!db.vitals[currentUserId]) {
      db.vitals[currentUserId] = {};
    }

    const currentVitals = db.vitals[currentUserId];

    if (type === 'BLOOD_PRESSURE') {
      currentVitals.bloodPressureSystolic = body.valuePrimary;
      currentVitals.bloodPressureDiastolic = body.valueSecondary || 80;
    } else if (type === 'BLOOD_SUGAR') {
      currentVitals.bloodSugar = body.valuePrimary;
    } else if (type === 'HEART_RATE') {
      currentVitals.heartRate = body.valuePrimary;
    } else if (type === 'WEIGHT') {
      currentVitals.weight = body.valuePrimary;
      if (currentVitals.height) {
        const hM = currentVitals.height / 100;
        const bmi = Math.round((body.valuePrimary / (hM * hM)) * 10) / 10;
        currentVitals.bmi = bmi;
        currentVitals.bmiCategory = bmi < 18.5 ? 'UNDERWEIGHT' : bmi < 25 ? 'NORMAL' : 'OVERWEIGHT';
      }
    } else if (type === 'HEIGHT') {
      currentVitals.height = body.valuePrimary;
      if (currentVitals.weight) {
        const hM = body.valuePrimary / 100;
        const bmi = Math.round((currentVitals.weight / (hM * hM)) * 10) / 10;
        currentVitals.bmi = bmi;
        currentVitals.bmiCategory = bmi < 18.5 ? 'UNDERWEIGHT' : bmi < 25 ? 'NORMAL' : 'OVERWEIGHT';
      }
    } else if (type === 'SPO2') {
      currentVitals.spo2 = body.valuePrimary;
    }

    db.vitalLogs.push({
      id: Date.now(),
      userId: currentUserId,
      recordType: type,
      valuePrimary: body.valuePrimary,
      valueSecondary: body.valueSecondary || null,
      readingTime: new Date().toISOString(),
      notes: body.notes || ''
    });

    saveDb(db);
    return { success: true, message: 'Measurement logged successfully', data: body };
  }

  // 9. Health Goals
  if (cleanUrl.endsWith('/api/v1/goals')) {
    if (method.toLowerCase() === 'post') {
      const body = typeof data === 'string' ? JSON.parse(data) : data || {};
      const newGoal = {
        id: Date.now(),
        patientId: currentUserId,
        ...body,
        currentValue: 0,
        progressPercentage: 0,
        status: 'IN_PROGRESS'
      };
      db.goals.push(newGoal);
      saveDb(db);
      return { success: true, data: newGoal };
    }
    return {
      success: true,
      data: db.goals.filter(g => g.patientId === currentUserId)
    };
  }

  // 10. Doctors & Appointments
  if (cleanUrl.match(/\/api\/v1\/doctors\/user\/\d+$/)) {
    const targetUserId = parseInt(cleanUrl.split('/')[5], 10);
    const doc = db.doctors.find(d => d.userId === targetUserId || d.id === targetUserId);
    return {
      success: true,
      data: doc || {
        id: targetUserId,
        userId: targetUserId,
        fullName: 'Dr. Physician',
        specializationName: 'General Medicine',
        qualification: 'MD',
        experienceYears: 5,
        consultationFee: 80
      }
    };
  }

  if (cleanUrl.match(/\/api\/v1\/doctors\/\d+$/)) {
    const targetDocId = parseInt(cleanUrl.split('/')[4], 10);
    const doc = db.doctors.find(d => d.id === targetDocId || d.userId === targetDocId);
    return { success: true, data: doc || db.doctors[0] };
  }

  if (cleanUrl.endsWith('/api/v1/doctors')) {
    return { success: true, data: db.doctors };
  }

  if (cleanUrl.endsWith('/api/v1/appointments/patient')) {
    return {
      success: true,
      data: db.appointments.filter(a => a.patientId === currentUserId)
    };
  }

  if (cleanUrl.match(/\/api\/v1\/appointments\/doctor\/\d+$/)) {
    const targetDocId = parseInt(cleanUrl.split('/')[5], 10);
    const docAppts = db.appointments.filter(a =>
      a.doctorId === targetDocId ||
      a.doctorId === Number(targetDocId) ||
      a.doctorUserId === targetDocId ||
      a.doctorUserId === Number(targetDocId) ||
      String(a.doctorId) === String(targetDocId)
    );
    return { success: true, data: docAppts };
  }

  if (cleanUrl.match(/\/api\/v1\/appointments\/\d+\/status$/)) {
    const apptId = parseInt(cleanUrl.split('/')[4], 10);
    const urlParams = new URLSearchParams(url.split('?')[1] || '');
    const newStatus = urlParams.get('status') || 'CONFIRMED';
    const notes = urlParams.get('doctorNotes') || '';

    const targetAppt = db.appointments.find(a => a.id === apptId);
    if (targetAppt) {
      targetAppt.status = newStatus;
      if (notes) targetAppt.doctorNotes = notes;
      saveDb(db);
    }
    return { success: true, message: `Appointment ${newStatus}`, data: targetAppt };
  }

  if (cleanUrl.endsWith('/api/v1/appointments') && method.toLowerCase() === 'post') {
    const body = typeof data === 'string' ? JSON.parse(data) : data || {};
    const targetDocId = Number(body.doctorId);
    const doc = db.doctors.find(d =>
      d.id === targetDocId ||
      d.userId === targetDocId ||
      d.id === body.doctorId ||
      d.userId === body.doctorId
    ) || db.doctors[0];

    let patientUser = db.users.find(u => u.id === currentUserId);
    if (!patientUser) {
      try {
        patientUser = JSON.parse(localStorage.getItem('medicare_user') || '{}');
      } catch (e) {
        patientUser = {};
      }
    }

    const newAppt = {
      id: Date.now(),
      doctorId: doc ? doc.id : targetDocId,
      doctorUserId: doc ? doc.userId : targetDocId,
      patientId: currentUserId,
      patientName: patientUser.fullName || 'Patient',
      patientEmail: patientUser.email || '',
      doctorName: doc ? doc.fullName : 'Physician',
      specialization: doc ? doc.specializationName : 'General Care',
      appointmentDate: body.appointmentDate,
      appointmentTime: body.appointmentTime,
      reason: body.reason || 'Medical consultation & prescription checkup',
      status: 'CONFIRMED',
      createdAt: new Date().toISOString()
    };
    db.appointments.unshift(newAppt);

    // Notify patient
    db.notifications.unshift({
      id: Date.now(),
      userId: currentUserId,
      title: 'Appointment Confirmed',
      message: `Your appointment with ${newAppt.doctorName} (${newAppt.specialization}) is confirmed for ${newAppt.appointmentDate} at ${newAppt.appointmentTime.substring(0, 5)}.`,
      notificationType: 'APPOINTMENT',
      isRead: false,
      createdAt: new Date().toISOString()
    });

    // Notify doctor
    if (doc?.userId) {
      db.notifications.unshift({
        id: Date.now() + 1,
        userId: doc.userId,
        title: 'New Patient Booking',
        message: `${newAppt.patientName} has booked a consultation with you for ${newAppt.appointmentDate} at ${newAppt.appointmentTime.substring(0, 5)}. Reason: ${newAppt.reason}`,
        notificationType: 'APPOINTMENT',
        isRead: false,
        createdAt: new Date().toISOString()
      });
    }

    saveDb(db);
    return { success: true, message: 'Appointment booked successfully', data: newAppt };
  }

  // 11. Notifications
  if (cleanUrl.endsWith('/api/v1/notifications/unread/count')) {
    const count = db.notifications.filter(n => n.userId === currentUserId && !n.isRead).length;
    return { success: true, data: count };
  }

  if (cleanUrl.endsWith('/api/v1/notifications')) {
    const userNotifs = db.notifications.filter(n => n.userId === currentUserId);
    return { success: true, data: { content: userNotifs } };
  }

  if (cleanUrl.endsWith('/api/v1/notifications/read-all')) {
    db.notifications.forEach(n => {
      if (n.userId === currentUserId) n.isRead = true;
    });
    saveDb(db);
    return { success: true, message: 'Marked all as read' };
  }

  // 12. Admin Users
  if (cleanUrl.endsWith('/api/v1/users')) {
    return { success: true, data: db.users };
  }

  return { success: true, message: 'OK', data: {} };
}
